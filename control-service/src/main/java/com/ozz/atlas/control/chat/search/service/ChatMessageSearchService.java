package com.ozz.atlas.control.chat.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.dto.ChatMessageDto;
import com.ozz.atlas.control.chat.repository.ChatMessageRepository;
import com.ozz.atlas.control.chat.search.document.ChatMessageDocument;
import com.ozz.atlas.control.chat.search.repository.ChatMessageSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageSearchService {

    private final ChatMessageSearchRepository chatMessageSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ChatMessageRepository chatMessageRepository;

    // 메시지 엔티티를 ES 문서로 저장
    // 메시지 전송, 수정, 삭제 후 계속 다시 저장
    @Transactional
    public void saveChatMessageDocument(ChatMessage chatMessage) {
        chatMessageSearchRepository.save(ChatMessageDocument.fromEntity(chatMessage));
    }

    // 메시지 검색
    // roomPublicId 를 같이 받아서 특정 방 안에서만 메시지를 검색하게 함
    public Page<ChatMessageDto> search(String roomPublicId, String keyword, Pageable pageable) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 특정 채팅방 안의 메시지만 검색
        if (hasText(roomPublicId)) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("roomPublicId")
                    .value(roomPublicId)
            )));
        }

        // 기본적으로 삭제되지 않은 메시지만 검색
        filterQueries.add(Query.of(q -> q.term(t -> t
                .field("status")
                .value(Status.ACTIVE.name())
        )));

        // keyword 는 메시지 본문, 참조 코드, 참조 제목을 대상으로 통합검색
        if (hasText(keyword)) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(keyword)
                    .fields(List.of(
                            "messageBody^3.0",
                            "messageBody.ngram^2.0",
                            "referenceCode^2.0",
                            "referenceCode.ngram^2.0",
                            "referenceTitle^2.0",
                            "referenceTitle.ngram^2.0"
                    ))
            )));
        }

        Query finalQuery = buildFinalQuery(mustQueries, filterQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<ChatMessageDocument> searchHits =
                elasticsearchOperations.search(query, ChatMessageDocument.class);

        List<ChatMessageDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toDto)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // DB 의 메시지 전체를 ES 에 다시 색인
    @Transactional
    public void reindexAllChatMessages() {
        chatMessageRepository.findAll().forEach(this::saveChatMessageDocument);
    }

    private ChatMessageDto toDto(ChatMessageDocument document) {
        return ChatMessageDto.builder()
                .publicId(document.getPublicId())
                .roomPublicId(document.getRoomPublicId())
                .senderUserPublicId(document.getSenderUserPublicId())
                .messageType(document.getMessageType())
                .messageBody(document.getStatus() == Status.DELETE ? "[삭제된 메시지입니다]" : document.getMessageBody())
                .referenceType(document.getReferenceType())
                .referencePublicId(document.getReferencePublicId())
                .referenceCode(document.getReferenceCode())
                .referenceTitle(document.getReferenceTitle())
                .attachmentPublicIds(document.getAttachmentPublicIds())
                .sentAt(document.getCreatedAt())
                .editedAt(document.getEditedAt())
                .isDeleted(document.getStatus() == Status.DELETE)
                .build();
    }

    private Query buildFinalQuery(List<Query> mustQueries, List<Query> filterQueries) {
        if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        return Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }
            return b;
        }));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

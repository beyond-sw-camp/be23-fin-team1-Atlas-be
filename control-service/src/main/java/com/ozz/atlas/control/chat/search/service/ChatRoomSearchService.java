package com.ozz.atlas.control.chat.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import com.ozz.atlas.control.chat.dto.ChatRoomDto;
import com.ozz.atlas.control.chat.repository.ChatMessageRepository;
import com.ozz.atlas.control.chat.repository.ChatParticipantRepository;
import com.ozz.atlas.control.chat.repository.ChatRoomRepository;
import com.ozz.atlas.control.chat.search.document.ChatRoomDocument;
import com.ozz.atlas.control.chat.search.repository.ChatRoomSearchRepository;
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
public class ChatRoomSearchService {

    private final ChatRoomSearchRepository chatRoomSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantSearchService chatParticipantSearchService;

    // 채팅방 엔티티를 ES 문서로 저장
    // 방 생성, 참여자 변경, 마지막 메시지 변경 시 계속 다시 저장
    @Transactional
    public void saveChatRoomDocument(ChatRoom chatRoom) {
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoomActive(chatRoom);
        ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByIdDesc(chatRoom).orElse(null);

        chatRoomSearchRepository.save(
                ChatRoomDocument.fromEntity(chatRoom, participants, lastMessage)
        );
    }

    // 채팅방 검색
    // userPublicId 를 같이 받는 이유는, 사용자가 참여 중인 방 안에서만 검색하게 하려는 목적
    public Page<ChatRoomDto> search(String userPublicId, String keyword, Pageable pageable) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 사용자가 참여 중인 채팅방만 검색 대상이 되도록 제한
        if (hasText(userPublicId)) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("participantUserPublicIds")
                    .value(userPublicId)
            )));
        }

        // keyword 는 방 이름 + 마지막 메시지를 중심으로 통합검색
        // 현재 participant 이름 필드는 없어서 이름 검색까지는 아직 못 함
        if (hasText(keyword)) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(keyword)
                    .fields(List.of(
                            "roomName^3.0",
                            "roomName.ngram^2.0",
                            "lastMessageBody^2.0",
                            "lastMessageBody.ngram^2.0"
                    ))
            )));
        }

        Query finalQuery = buildFinalQuery(mustQueries, filterQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<ChatRoomDocument> searchHits =
                elasticsearchOperations.search(query, ChatRoomDocument.class);

        List<ChatRoomDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(document -> toDto(document, userPublicId))
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // DB 에 있는 채팅방 전체를 ES 에 다시 색인
    @Transactional
    public void reindexAllChatRooms() {
        chatRoomRepository.findAll().forEach(this::saveChatRoomDocument);
    }

    private ChatRoomDto toDto(ChatRoomDocument document, String userPublicId) {
        Long unreadCount = 0L;

        // 검색 결과도 일반 목록 조회와 동일하게 안 읽은 메시지 수를 계산합니다.
        if (hasText(userPublicId) && document.getPublicId() != null) {
            ChatRoom chatRoom = chatRoomRepository.findByPublicId(document.getPublicId()).orElse(null);

            if (chatRoom != null) {
                ChatParticipant participant = chatParticipantRepository
                        .findByChatRoomAndUserPublicId(chatRoom, userPublicId)
                        .orElse(null);

                if (participant != null) {
                    unreadCount = chatMessageRepository.countUnreadMessages(
                            chatRoom,
                            participant.getLastReadMessageId()
                    );
                }
            }
        }

        List<com.ozz.atlas.control.chat.dto.ChatParticipantDto> participantDtos = chatParticipantSearchService.getParticipantsByRoomPublicId(document.getPublicId())
                .stream()
                .map(doc -> com.ozz.atlas.control.chat.dto.ChatParticipantDto.builder()
                        .userPublicId(doc.getUserPublicId())
                        .displayName(doc.getDisplayName())
                        .profileImageThumbPath(doc.getProfileImageThumbPath())
                        .build())
                .toList();

        return ChatRoomDto.builder()
                .publicId(document.getPublicId())
                .roomName(document.getRoomName())
                .roomStatus(document.getRoomStatus())
                .userAccountPublicId(document.getUserAccountPublicId())
                .createdAt(document.getCreatedAt())
                .unreadCount(unreadCount)
                .lastMessage(document.getLastMessageBody())
                .lastMessageAt(document.getLastMessageAt())
                .participants(participantDtos)
                .build();
    }


    private Query buildFinalQuery(List<Query> mustQueries, List<Query> filterQueries) {
        // 조건이 아무것도 없으면 전체 조회로 처리
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

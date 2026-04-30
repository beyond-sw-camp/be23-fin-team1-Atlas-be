package com.ozz.atlas.control.chat.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.chat.repository.ChatParticipantRepository;
import com.ozz.atlas.control.chat.search.document.ChatParticipantDocument;
import com.ozz.atlas.control.chat.search.repository.ChatParticipantSearchRepository;
import com.ozz.atlas.control.client.AuthServiceClient;
import com.ozz.atlas.control.client.dto.AuthUserDetailDto;
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
public class ChatParticipantSearchService {

    private final ChatParticipantSearchRepository chatParticipantSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ChatParticipantRepository chatParticipantRepository;
    private final AuthServiceClient authServiceClient;

    // 참여자 엔티티를 Elasticsearch 문서로 저장
    // 저장할 때 auth-service 에서 사용자 상세정보를 같이 조회해서
    // 이름, 로그인아이디, 이메일까지 검색 문서에 함께 넣음
    @Transactional
    public void saveChatParticipantDocument(ChatParticipant participant) {
        AuthUserDetailDto user = authServiceClient.getUserDetailByPublicId(participant.getUserPublicId());
        chatParticipantSearchRepository.save(ChatParticipantDocument.fromEntity(participant, user));
    }

    // 채팅방 참여자 검색
    // 특정 방 안에서 사람 이름, 로그인아이디, 이메일 기준으로 검색
    public Page<ChatParticipantDocument> search(String roomPublicId, String keyword, Pageable pageable) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 특정 채팅방 안의 참여자만 검색 대상으로 제한
        if (hasText(roomPublicId)) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("roomPublicId")
                    .value(roomPublicId)
            )));
        }

        // 현재 활성 상태인 참여자만 검색
        // 채팅방을 나간 비활성 참여자는 검색 결과에서 제외
        filterQueries.add(Query.of(q -> q.term(t -> t
                .field("activeYn")
                .value(true)
        )));

        // 이름, 로그인아이디, 이메일은 ngram 필드를 사용해서
        // 일부 문자열만 입력해도 검색되게 함
        if (hasText(keyword)) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(keyword)
                    .fields(List.of(
                            "displayName^3.0",
                            "displayName.ngram^2.0",
                            "loginId^2.0",
                            "loginId.ngram^2.0",
                            "email^2.0",
                            "email.ngram^2.0"
                    ))
            )));
        }

        Query finalQuery = buildFinalQuery(mustQueries, filterQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<ChatParticipantDocument> searchHits =
                elasticsearchOperations.search(query, ChatParticipantDocument.class);

        List<ChatParticipantDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // DB 의 참여자 전체를 Elasticsearch 에 다시 색인
    // 인덱스를 새로 만들었을 때 초기 적재용으로 사용
    @Transactional
    public void reindexAllChatParticipants() {
        chatParticipantRepository.findAll().forEach(this::saveChatParticipantDocument);
    }

    public List<ChatParticipantDocument> getParticipantsByRoomPublicId(String roomPublicId) {
        return chatParticipantSearchRepository.findByRoomPublicIdAndActiveYnTrue(roomPublicId);
    }

    // must 조건과 filter 조건을 합쳐 최종 bool 쿼리를 만듬
    // 조건이 아무것도 없으면 matchAll 로 전체 조회
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

    // 문자열 값이 null 이거나 공백인지 확인하는 공통 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

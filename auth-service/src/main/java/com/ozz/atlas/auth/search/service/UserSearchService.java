package com.ozz.atlas.auth.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.UserListDto;
import com.ozz.atlas.auth.dtos.UserSearchDto;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.auth.search.document.UserDocument;
import com.ozz.atlas.auth.search.repository.UserSearchRepository;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserSearchService {

    private final UserSearchRepository userSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final UserRepository userRepository;

    public UserSearchService(UserSearchRepository userSearchRepository,
                             ElasticsearchOperations elasticsearchOperations,
                             UserRepository userRepository) {
        this.userSearchRepository = userSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.userRepository = userRepository;
    }

    // 사용자 엔티티를 Elasticsearch 문서로 저장
    // 회원가입, 수정, 권한변경, 삭제(상태변경) 후에 계속 재동기화할 때 사용
    public void saveUserDocument(User user) {
        userSearchRepository.save(UserDocument.fromEntity(user));
    }

    // Elasticsearch 에서 사용자 문서를 삭제
    // 자금은 소프트딜리트라서 자주 쓰이진 않지만,필요할 때를 대비해 유지
    public void deleteUserDocument(Long userId) {
        userSearchRepository.deleteById(userId);
    }

    public Page<UserListDto> search(Pageable pageable, UserSearchDto searchDto) {
        // mustQueries:
        // 실제 검색어 조건들
        // 예: keyword, loginId, firstName 같은 "무엇을 찾을지"에 해당
        List<Query> mustQueries = new ArrayList<>();

        // filterQueries:
        // 결과를 좁히는 정확한 조건들
        // 예: 조직, 권한, 상태처럼 "정확히 일치해야 하는 값"을 넣음
        List<Query> filterQueries = new ArrayList<>();

        // 조직 publicId가 들어오면 해당 조직 사용자만 보이게 필터링
        if (hasText(searchDto.getOrganizationPublicId())) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("organizationPublicId.keyword")
                    .value(searchDto.getOrganizationPublicId())
            )));
        }

        // 권한(ADMIN, USER 등)이 들어오면 정확히 그 권한만 조회
        if (searchDto.getUserRole() != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("userRole.keyword")
                    .value(searchDto.getUserRole().name())
            )));
        }

        // 상태는 값이 안 들어오면 기본적으로 ACTIVE만 조회
        // 기존 DB 조회 로직도 같은 정책이었으므로 맞춤
        Status status = searchDto.getStatus() != null ? searchDto.getStatus() : Status.ACTIVE;
        filterQueries.add(Query.of(q -> q.term(t -> t
                .field("status.keyword")
                .value(status.name())
        )));

        // keyword는 "통합검색창" 용도
        // 한 번의 입력으로 아이디, 이름, 이메일, 전화번호, 직책을 같이 검색
        // ngram 필드를 같이 넣어서 부분검색도 가능하게 함
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(searchDto.getKeyword())
                    .fields(List.of(
                            "loginId^3.0",
                            "loginId.ngram^2.0",
                            "firstName^2.0",
                            "firstName.ngram^2.0",
                            "middleName",
                            "middleName.ngram",
                            "lastName^2.0",
                            "lastName.ngram^2.0",
                            "email^2.0",
                            "email.ngram^2.0",
                            "phone",
                            "phone.ngram^2.0",
                            "jobTitle",
                            "jobTitle.ngram"
                    ))
            )));
        }

        // 아래부터는 상세검색 필드들
        // 개별 필드 검색도 부분검색이 되도록 ngram 필드를 사용

        // 로그인 아이디 일부만 입력해도 검색 가능하게 함
        addPartialMatchIfPresent(mustQueries, "loginId.ngram", searchDto.getLoginId());

        // 이름 일부만 입력해도 검색 가능하게 함
        addPartialMatchIfPresent(mustQueries, "firstName.ngram", searchDto.getFirstName());
        addPartialMatchIfPresent(mustQueries, "middleName.ngram", searchDto.getMiddleName());
        addPartialMatchIfPresent(mustQueries, "lastName.ngram", searchDto.getLastName());

        // 최종 bool 쿼리를 조립
        // must: 검색어 조건
        // filter: 정확히 맞아야 하는 조건
        Query finalQuery = Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }
            return b;
        }));

        // pageable을 그대로 넘겨서 페이지 번호, 크기, 정렬을 유지
        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        // Elasticsearch 에 실제 검색을 요청
        SearchHits<UserDocument> searchHits =
                elasticsearchOperations.search(query, UserDocument.class);

        // 검색 결과 문서를 API 응답 DTO로 변환
        List<UserListDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(UserListDto::fromDocument)
                .toList();

        // Elasticsearch 검색 결과를 Spring Page 형태로 감싸서 반환
        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // DB 사용자 전체를 ES에 다시 밀어넣는 재색인 메서드
    // users 인덱스를 새로 만들었을 때 초기 데이터 적재에 사용
    public void reindexAllUsers() {
        userRepository.findAllWithOrganizationBy().forEach(user ->
                userSearchRepository.save(UserDocument.fromEntity(user))
        );
    }

    // 문자열이 null 이거나 공백인지 검사하는 공통 메서드
    // if 문에서 반복적으로 쓰는 코드를 줄이기 위해 분리
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // 부분검색용 공통 메서드
    // 값이 들어왔을 때만 match 쿼리를 추가
    // ngram 필드를 대상으로 검색하므로 일부 문자열만 입력해도 찾을 수 있음
    private void addPartialMatchIfPresent(List<Query> mustQueries, String field, String value) {
        if (!hasText(value)) {
            return;
        }

        mustQueries.add(Query.of(q -> q.match(m -> m
                .field(field)
                .query(value)
        )));
    }
}

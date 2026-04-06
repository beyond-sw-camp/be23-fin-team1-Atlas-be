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
import org.springframework.data.domain.PageRequest;
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
                             ElasticsearchOperations elasticsearchOperations, UserRepository userRepository) {
        this.userSearchRepository = userSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.userRepository = userRepository;
    }

    public void saveUserDocument(User user) {
        userSearchRepository.save(UserDocument.fromEntity(user));
    }

    public void deleteUserDocument(Long userId) {
        userSearchRepository.deleteById(userId);
    }

    public Page<UserListDto> search(Pageable pageable, UserSearchDto searchDto) {
        List<Query> filters = new ArrayList<>();

        if (searchDto.getOrganizationPublicId() != null && !searchDto.getOrganizationPublicId().isBlank()) {
            filters.add(Query.of(q -> q.term(t -> t.field("organizationPublicId")
                    .value(searchDto.getOrganizationPublicId()))));
        }

        if (searchDto.getUserRole() != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("userRole")
                    .value(searchDto.getUserRole().name()))));
        }

        Status status = searchDto.getStatus() != null ? searchDto.getStatus() : Status.ACTIVE;
        filters.add(Query.of(q -> q.term(t -> t.field("status")
                .value(status.name()))));

        Query keywordQuery = Query.of(q -> q.multiMatch(m -> m
                .query(searchDto.getKeyword())
                .fields(List.of("loginId", "firstName", "middleName", "lastName", "email"))
        ));

        Query finalQuery = Query.of(q -> q.bool(b -> b
                .must(keywordQuery)
                .filter(filters)
        ));

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .build();

        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(query, UserDocument.class);

        List<UserListDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(UserListDto::fromDocument)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    public void reindexAllUsers() {
        userRepository.findAll().forEach(user ->
                userSearchRepository.save(UserDocument.fromEntity(user))
        );
    }

}

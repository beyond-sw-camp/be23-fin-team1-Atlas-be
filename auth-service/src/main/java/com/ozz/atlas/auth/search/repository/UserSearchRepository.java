package com.ozz.atlas.auth.search.repository;

import com.ozz.atlas.auth.search.document.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Long> {
}

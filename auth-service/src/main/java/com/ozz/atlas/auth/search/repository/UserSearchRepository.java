package com.ozz.atlas.auth.search.repository;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.search.document.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Long> {

}

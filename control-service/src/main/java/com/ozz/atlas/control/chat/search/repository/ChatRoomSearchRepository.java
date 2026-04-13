package com.ozz.atlas.control.chat.search.repository;

import com.ozz.atlas.control.chat.search.document.ChatRoomDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomSearchRepository extends ElasticsearchRepository<ChatRoomDocument, Long> {
}

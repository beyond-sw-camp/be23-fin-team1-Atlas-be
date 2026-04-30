package com.ozz.atlas.control.chat.search.repository;


import com.ozz.atlas.control.chat.search.document.ChatParticipantDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatParticipantSearchRepository extends ElasticsearchRepository<ChatParticipantDocument, Long> {

    List<ChatParticipantDocument> findByRoomPublicIdAndActiveYnTrue(String roomPublicId);

}

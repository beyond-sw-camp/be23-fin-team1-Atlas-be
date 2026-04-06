package com.ozz.atlas.control.chat.repository;

import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);
    
    // 커서 기반 페이지네이션을 위한 쿼리
    Page<ChatMessage> findByChatRoomAndIdLessThanOrderByIdDesc(ChatRoom chatRoom, Long id, Pageable pageable);
    
    Page<ChatMessage> findByChatRoomOrderByIdDesc(ChatRoom chatRoom, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :chatRoom AND m.id > COALESCE(:lastReadMessageId, 0)")
    Long countUnreadMessages(@Param("chatRoom") ChatRoom chatRoom, @Param("lastReadMessageId") Long lastReadMessageId);

    Optional<ChatMessage> findTopByChatRoomOrderByIdDesc(ChatRoom chatRoom);
    
    Optional<ChatMessage> findByPublicId(String publicId);
}

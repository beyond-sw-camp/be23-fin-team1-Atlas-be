package com.ozz.atlas.control.chat.repository;

import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    // 활성 상태(active_yn = true)인 참여자만 조회
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom = :chatRoom AND cp.activeYn = true")
    List<ChatParticipant> findByChatRoomActive(@Param("chatRoom") ChatRoom chatRoom);
    
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.userPublicId = :userPublicId AND cp.activeYn = true")
    List<ChatParticipant> findByUserPublicIdActive(@Param("userPublicId") String userPublicId);
    
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom = :chatRoom AND cp.userPublicId = :userPublicId AND cp.activeYn = true")
    Optional<ChatParticipant> findByChatRoomAndUserPublicIdActive(@Param("chatRoom") ChatRoom chatRoom, @Param("userPublicId") String userPublicId);

    // 단순 조회용 (나가기 전 상태 확인 등)
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    List<ChatParticipant> findByUserPublicId(String userPublicId);
    Optional<ChatParticipant> findByChatRoomAndUserPublicId(ChatRoom chatRoom, String userPublicId);

    @Modifying
    @Query("UPDATE ChatParticipant cp SET cp.lastReadMessageId = :messageId WHERE cp.chatRoom = :chatRoom AND cp.userPublicId IN :userPublicIds AND cp.activeYn = true")
    void updateLastReadMessageIdForUsers(@Param("chatRoom") ChatRoom chatRoom, @Param("userPublicIds") List<String> userPublicIds, @Param("messageId") Long messageId);

    // 채팅방 목록 페이지 조회용
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.userPublicId = :userPublicId AND cp.activeYn = true")
    Page<ChatParticipant> findByUserPublicIdActive(@Param("userPublicId") String userPublicId, Pageable pageable);

    @Query("""
            SELECT COUNT(cp) FROM ChatParticipant cp
            WHERE cp.chatRoom = :chatRoom
              AND cp.activeYn = true
              AND (cp.lastReadMessageId IS NULL OR cp.lastReadMessageId < :messageId)
              AND cp.createdAt <= (SELECT cm.createdAt FROM ChatMessage cm WHERE cm.id = :messageId)
              AND cp.visibleFromAt <= (SELECT cm.createdAt FROM ChatMessage cm WHERE cm.id = :messageId)
            """)
    long countUnreadParticipants(@Param("chatRoom") ChatRoom chatRoom, @Param("messageId") Long messageId);
}

package com.ozz.atlas.control.chat.repository;

import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    List<ChatParticipant> findByUserPublicId(String userPublicId);

    @Modifying
    @Query("UPDATE ChatParticipant cp SET cp.lastReadMessageId = :messageId WHERE cp.chatRoom = :chatRoom AND cp.userPublicId IN :userPublicIds")
    void updateLastReadMessageIdForUsers(@Param("chatRoom") ChatRoom chatRoom, @Param("userPublicIds") List<String> userPublicIds, @Param("messageId") Long messageId);
}


package com.ozz.atlas.control.chat.repository;

import com.ozz.atlas.control.chat.domain.ChatRoom;
import com.ozz.atlas.control.chat.domain.ChatRoomPin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomPinRepository extends JpaRepository<ChatRoomPin, Long> {
    Optional<ChatRoomPin> findByChatRoomAndUserPublicId(ChatRoom chatRoom, String userPublicId);
    List<ChatRoomPin> findByUserPublicId(String userPublicId);
    void deleteByChatRoomAndUserPublicId(ChatRoom chatRoom, String userPublicId);
}
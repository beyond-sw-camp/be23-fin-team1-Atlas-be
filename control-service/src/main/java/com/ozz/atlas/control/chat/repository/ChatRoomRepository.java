package com.ozz.atlas.control.chat.repository;

import com.ozz.atlas.control.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByPublicId(String publicId);
}

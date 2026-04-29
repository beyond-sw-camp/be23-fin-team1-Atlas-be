package com.ozz.atlas.control.chat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_pin", uniqueConstraints = {
        @UniqueConstraint(name = "uk_room_user", columnNames = {"room_id", "user_public_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomPin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "user_public_id", nullable = false)
    private String userPublicId;

    @CreationTimestamp
    @Column(name = "pinned_at", nullable = false)
    private LocalDateTime pinnedAt;

    @Builder
    public ChatRoomPin(ChatRoom chatRoom, String userPublicId) {
        this.chatRoom = chatRoom;
        this.userPublicId = userPublicId;
    }
}

package com.ozz.atlas.control.chat.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.control.chat.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @Column(name = "public_id", nullable = false, length = 26, unique = true)
    private String publicId;

    @Column(name = "room_name", nullable = false, length = 200)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false)
    private RoomStatus roomStatus;

    @Column(name = "user_account_public_id", nullable = false, length = 26)
    private String userAccountPublicId;

    @PrePersist
    public void prePersist() {
        if (this.roomStatus == null) {
            this.roomStatus = RoomStatus.OPEN;
        }
    }

    public void updateRoomName(String roomName) {
        this.roomName = roomName;
    }
}

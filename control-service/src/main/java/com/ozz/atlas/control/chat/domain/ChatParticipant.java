package com.ozz.atlas.control.chat.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "user_public_id", length = 26)
    private String userPublicId;

    @Column(name = "organization_public_id", length = 26)
    private String organizationPublicId;

    @Column(name = "participant_role", length = 30)
    private String participantRole;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "active_yn", nullable = false)
    private boolean activeYn;

    @PrePersist
    public void prePersist() {
        this.activeYn = true;
    }
}

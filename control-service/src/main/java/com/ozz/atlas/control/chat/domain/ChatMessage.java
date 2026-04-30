package com.ozz.atlas.control.chat.domain;

import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.jpa.SoftDeleteEntity;
import com.ozz.atlas.control.chat.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @Column(name = "public_id", nullable = false, length = 26, unique = true)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "sender_user_public_id", length = 26)
    private String senderUserPublicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(name = "message_body", columnDefinition = "TEXT")
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30)
    private DomainType referenceType;

    @Column(name = "reference_public_id", length = 26)
    private String referencePublicId;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @Column(name = "reference_title", length = 200)
    private String referenceTitle;

    @Column(name = "attachment_public_ids", columnDefinition = "TEXT")
    private String attachmentPublicIds;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "parent_message_public_id", length = 26)
    private String parentMessagePublicId;

    @Column(name = "parent_message_body", length = 100)
    private String parentMessageBody;

    @Column(name = "parent_sender_user_public_id", length = 26)
    private String parentSenderUserPublicId;

    @Column(name = "parent_sender_display_name", length = 100)
    private String parentSenderDisplayName;

    @PrePersist
    public void prePersist() {
        if (this.messageType == null) {
            this.messageType = MessageType.TEXT;
        }
    }

    public void updateMessage(String messageBody) {
        this.messageBody = messageBody;
        this.editedAt = LocalDateTime.now();
    }
}

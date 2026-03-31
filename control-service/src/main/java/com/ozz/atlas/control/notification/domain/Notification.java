package com.ozz.atlas.control.notification.domain;

import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.jpa.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "public_id", nullable = false, length = 26, unique = true)
    private String publicId;

    @Column(name = "recipient_user_public_id", nullable = false, length = 26)
    private String recipientUserPublicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private DomainType notificationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "deep_link_url", length = 500)
    private String deepLinkUrl;

    @Column(name = "read_yn", nullable = false)
    private boolean readYn;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "reference_public_id", length = 26)
    private String referencePublicId;

    @PrePersist
    public void prePersist() {
        this.readYn = false;
    }

    public void markAsRead() {
        if (!this.readYn) {
            this.readYn = true;
            this.readAt = LocalDateTime.now();
        }
    }
}

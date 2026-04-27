package com.ozz.atlas.control.notification.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_notification_preference",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_notification_preference_user_category",
                columnNames = {"user_public_id", "category"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserNotificationPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_notification_preference_id")
    private Long id;

    @Column(name = "user_public_id", nullable = false, length = 26)
    private String userPublicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private NotificationCategory category;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    public static UserNotificationPreference create(
            String userPublicId,
            NotificationCategory category,
            boolean enabled
    ) {
        return UserNotificationPreference.builder()
                .userPublicId(userPublicId)
                .category(category)
                .enabled(enabled)
                .build();
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

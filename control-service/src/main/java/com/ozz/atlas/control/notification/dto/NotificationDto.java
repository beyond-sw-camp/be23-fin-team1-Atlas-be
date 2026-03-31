package com.ozz.atlas.control.notification.dto;

import com.ozz.atlas.common.domain.DomainType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String publicId;
    private String recipientUserPublicId;
    private DomainType notificationType;
    private String title;
    private String message;
    private String deepLinkUrl;
    private String referencePublicId;
    private boolean readYn;
    private LocalDateTime createdAt;
}

package com.ozz.atlas.control.notification.command;

import com.ozz.atlas.common.domain.DomainType;
import lombok.Builder;

@Builder
public record NotificationCommand(
        String recipientUserPublicId,
        DomainType notificationType,
        String title,
        String message,
        String deepLinkUrl,
        String referencePublicId
) {
}

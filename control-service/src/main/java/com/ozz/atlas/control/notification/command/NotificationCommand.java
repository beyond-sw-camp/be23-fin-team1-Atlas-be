package com.ozz.atlas.control.notification.command;

import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.control.notification.domain.NotificationToastType;
import lombok.Builder;

@Builder
public record NotificationCommand(
        String recipientUserPublicId,
        String eventType,
        DomainType domainType,
        NotificationToastType toastType,
        String title,
        String message,
        String deepLinkUrl,
        String referencePublicId
) {
}

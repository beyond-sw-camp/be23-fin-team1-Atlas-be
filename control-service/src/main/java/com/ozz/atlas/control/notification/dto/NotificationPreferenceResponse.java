package com.ozz.atlas.control.notification.dto;

import com.ozz.atlas.control.notification.domain.NotificationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사용자 알림 설정 응답")
public record NotificationPreferenceResponse(
        @Schema(description = "알림 카테고리", example = "PURCHASE_ORDER")
        String category,
        @Schema(description = "화면 표시명", example = "발주")
        String label,
        @Schema(description = "카테고리 설명", example = "발주 생성, 수정, 확정, 수락, 거절, 취소 알림")
        String description,
        @Schema(description = "사용자 설정 가능 여부", example = "true")
        boolean userConfigurable,
        @Schema(description = "알림 수신 여부", example = "true")
        boolean enabled,
        @Schema(description = "화면 표시 순서", example = "1")
        int displayOrder
) {
    public static NotificationPreferenceResponse of(NotificationCategory category, boolean enabled) {
        return NotificationPreferenceResponse.builder()
                .category(category.name())
                .label(category.getLabel())
                .description(category.getDescription())
                .userConfigurable(category.isUserConfigurable())
                .enabled(enabled)
                .displayOrder(category.getDisplayOrder())
                .build();
    }
}

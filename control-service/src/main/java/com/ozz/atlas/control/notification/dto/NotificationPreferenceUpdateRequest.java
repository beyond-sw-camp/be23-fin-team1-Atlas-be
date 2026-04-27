package com.ozz.atlas.control.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 알림 설정 변경 요청")
public record NotificationPreferenceUpdateRequest(
        @NotNull(message = "enabled 값은 필수입니다.")
        @Schema(description = "알림 수신 여부", example = "false")
        Boolean enabled
) {
}

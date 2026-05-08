package com.ozz.atlas.auth.dtos.history;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.auth.domain.LoginHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Login History List 값 모델")
public class LoginHistoryListDto {
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long loginHistoryId;

    @Schema(description = "식별자", example = "1", nullable = true)
    private Long userId;

    @Schema(description = "login At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime loginAt;

    @Schema(description = "사유", example = "샘플 내용", nullable = true)
    private String failureReason;

    @Schema(description = "ip Address 값", example = "sample", nullable = true)
    private String ipAddress;

    @Schema(description = "user Agent 값", example = "sample", nullable = true)
    private String userAgent;

    @Schema(description = "사용자 공개 식별자", example = "sample_public_id", nullable = true)
    private String userPublicId;
    public static LoginHistoryListDto fromEntity(LoginHistory loginHistory) {
        return LoginHistoryListDto.builder()
                .loginHistoryId(loginHistory.getLoginHistoryId())
                .loginAt(loginHistory.getCreatedAt())
                .failureReason(loginHistory.getFailureReason())
                .ipAddress(loginHistory.getIpAddress())
                .userAgent(loginHistory.getUserAgent())
                .userId(loginHistory.getUser().getUserId())
                .userPublicId(loginHistory.getUser().getPublicId())
                .build();
    }

}

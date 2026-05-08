package com.ozz.atlas.auth.dtos.history;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.auth.domain.SecurityHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Security History List 값 모델")
public class SecurityHistoryListDto {

    @Schema(description = "식별자", example = "1", nullable = true)
    private Long securityHistoryId;

    @Schema(description = "식별자", example = "1", nullable = true)
    private Long userId;

    @Schema(description = "사용자 공개 식별자", example = "sample_public_id", nullable = true)
    private String userPublicId;

    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String eventType;

    @Schema(description = "summary 값", example = "sample", nullable = true)
    private String summary;

    @Schema(description = "ip Address 값", example = "sample", nullable = true)
    private String ipAddress;

    @Schema(description = "user Agent 값", example = "sample", nullable = true)
    private String userAgent;

    @Schema(description = "occurred At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime occurredAt;
    public static SecurityHistoryListDto fromEntity(SecurityHistory history) {
        return SecurityHistoryListDto.builder()
                .securityHistoryId(history.getSecurityHistoryId())
                .userId(history.getUser().getUserId())
                .userPublicId(history.getUser().getPublicId())
                .eventType(history.getEventType())
                .summary(history.getSummary())
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .occurredAt(history.getCreatedAt())
                .build();
    }
}

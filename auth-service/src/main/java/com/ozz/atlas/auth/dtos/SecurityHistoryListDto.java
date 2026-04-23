package com.ozz.atlas.auth.dtos;

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
public class SecurityHistoryListDto {

    private Long securityHistoryId;

    private Long userId;

    private String userPublicId;

    private String eventType;

    private String summary;

    private String ipAddress;

    private String userAgent;

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

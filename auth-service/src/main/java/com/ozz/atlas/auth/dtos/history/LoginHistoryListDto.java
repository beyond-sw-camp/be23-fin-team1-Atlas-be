package com.ozz.atlas.auth.dtos.history;

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
public class LoginHistoryListDto {
    private Long loginHistoryId;

    private Long userId;

    private LocalDateTime loginAt;

    private String failureReason;

    private String ipAddress;

    private String userAgent;

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

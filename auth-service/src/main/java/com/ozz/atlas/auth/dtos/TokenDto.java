package com.ozz.atlas.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "로그인 성공 또는 추가 인증 필요 시 반환되는 응답")
public class TokenDto {

    @Schema(description = "API 호출에 사용하는 Access Token", example = "eyJhbGciOiJIUzI1NiJ9.access-token")
    private String accessToken;

    @Schema(description = "Access Token 재발급에 사용하는 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.refresh-token")
    private String refreshToken;

    @Schema(description = "첫 로그인 후 비밀번호 변경 필요 여부", example = "true")
    private boolean passwordChangeRequired;

    // 새 IP 이면 이메일 인증 단계로 넘길지 여부
    @Schema(description = "새 IP 로그인으로 이메일 인증이 필요한지 여부", example = "true")
    private boolean ipVerificationRequired;

    // 새 IP 인증 요청 ID
    @Schema(description = "새 IP 인증 요청 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String verificationRequestId;

    // 새 IP 인증 만료 시각
    @Schema(description = "새 IP 인증 만료 시각", example = "2026-04-23T15:30:00")
    private LocalDateTime verificationExpiresAt;
}

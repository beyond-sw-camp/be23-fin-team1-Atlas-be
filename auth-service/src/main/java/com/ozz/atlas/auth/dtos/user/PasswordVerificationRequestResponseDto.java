package com.ozz.atlas.auth.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Password Verification Request 값 응답")
public class PasswordVerificationRequestResponseDto {

    // 인증코드 확인 단계에서 다시 보낼 요청 ID
    @Schema(description = "식별자", example = "1", nullable = true)
    private String verificationRequestId;

    // 인증코드 만료 시각
    @Schema(description = "expires At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime expiresAt;
}

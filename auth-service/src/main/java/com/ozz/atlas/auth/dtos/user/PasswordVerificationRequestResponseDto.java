package com.ozz.atlas.auth.dtos.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordVerificationRequestResponseDto {

    // 인증코드 확인 단계에서 다시 보낼 요청 ID
    private String verificationRequestId;

    // 인증코드 만료 시각
    private LocalDateTime expiresAt;
}

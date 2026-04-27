package com.ozz.atlas.auth.dtos.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordVerificationConfirmDto {

    // 어떤 비밀번호 변경 인증 요청인지 구분하는 ID
    @NotBlank
    private String verificationRequestId;

    // 이메일로 받은 6자리 인증코드
    @NotBlank
    private String verificationCode;
}

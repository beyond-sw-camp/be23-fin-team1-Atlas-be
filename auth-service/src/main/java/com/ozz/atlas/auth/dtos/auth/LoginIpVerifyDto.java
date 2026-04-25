package com.ozz.atlas.auth.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginIpVerifyDto {

    // 로그인 시 받은 인증 요청 ID
    @NotBlank(message = "인증 요청 ID 는 비어 있을 수 없습니다.")
    private String verificationRequestId;

    // 이메일로 받은 인증 코드
    @NotBlank(message = "인증 코드는 비어 있을 수 없습니다.")
    private String verificationCode;
}

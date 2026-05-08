package com.ozz.atlas.auth.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Password Verification Confirm 값 모델")
public class PasswordVerificationConfirmDto {

    // 어떤 비밀번호 변경 인증 요청인지 구분하는 ID
    @NotBlank
    @Schema(description = "식별자", example = "1")
    private String verificationRequestId;

    // 이메일로 받은 6자리 인증코드
    @NotBlank
    @Schema(description = "코드", example = "CODE-001")
    private String verificationCode;
}

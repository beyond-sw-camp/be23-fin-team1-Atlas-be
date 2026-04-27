package com.ozz.atlas.auth.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Access Token 재발급 요청")
public class RefreshTokenRequestDto {
    @NotBlank(message = "refresh token은 필수입니다.")
    @Schema(description = "이전에 발급받은 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.refresh-token")
    private String refreshToken;
}

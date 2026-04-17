package com.ozz.atlas.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "로그인 성공 시 반환되는 토큰 정보")
public class TokenDto {
    @Schema(description = "API 호출에 사용하는 Access Token", example = "eyJhbGciOiJIUzI1NiJ9.access-token")
    private String accessToken;

    @Schema(description = "Access Token 재발급에 사용하는 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.refresh-token")
    private String refreshToken;
}

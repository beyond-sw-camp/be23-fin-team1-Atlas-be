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
@Schema(description = "토큰 재발급 응답")
public class AccessTokenResponseDto {
    @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzI1NiJ9.new-access-token")
    private String accessToken;
}

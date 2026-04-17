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
@Schema(description = "회원가입 응답")
public class UserCreateResponseDto {
    @Schema(description = "생성된 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String userPublicId;
}

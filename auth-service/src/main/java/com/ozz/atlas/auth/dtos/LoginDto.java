package com.ozz.atlas.auth.dtos;

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
@Schema(description = "로그인 요청")
public class LoginDto {
    @NotBlank(message = "로그인 아이디는 비어있으면 안 됩니다.")
    @Schema(description = "사용자 로그인 ID", example = "atlas_admin")
    private String loginId;

    @NotBlank(message = "비밀번호는 비어있으면 안 됩니다.")
    @Schema(description = "사용자 비밀번호", example = "Atlas!234")
    private String password;
}

package com.ozz.atlas.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDto {
    @NotBlank(message = "로그인 아이디는 비어있으면 안 됩니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 비어있으면 안 됩니다.")
    private String password;
}

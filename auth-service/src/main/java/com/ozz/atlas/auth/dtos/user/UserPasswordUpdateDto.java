package com.ozz.atlas.auth.dtos.user;

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
@Schema(description = "날짜 모델")
public class UserPasswordUpdateDto {

    @Schema(description = "current Password 값", example = "sample", nullable = true)
    private String currentPassword;

    @NotBlank
    @Schema(description = "new Password 값", example = "sample")
    private String newPassword;

    @NotBlank
    @Schema(description = "new Password Confirm 값", example = "sample")
    private String newPasswordConfirm;
}

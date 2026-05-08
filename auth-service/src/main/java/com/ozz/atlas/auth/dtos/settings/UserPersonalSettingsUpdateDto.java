package com.ozz.atlas.auth.dtos.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "날짜 모델")
public class UserPersonalSettingsUpdateDto {

    @NotBlank
    @Schema(description = "language 값", example = "sample")
    private String language;
}

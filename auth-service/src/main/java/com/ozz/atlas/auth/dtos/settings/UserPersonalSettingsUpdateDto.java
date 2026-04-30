package com.ozz.atlas.auth.dtos.settings;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserPersonalSettingsUpdateDto {

    @NotBlank
    private String language;
}

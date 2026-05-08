package com.ozz.atlas.auth.dtos.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.auth.domain.UserPersonalSettings;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "User Personal Settings 값 응답")
public class UserPersonalSettingsResponseDto {

    @Schema(description = "language 값", example = "sample", nullable = true)
    private String language;
    public static UserPersonalSettingsResponseDto from(UserPersonalSettings settings) {
        return UserPersonalSettingsResponseDto.builder()
                .language(settings.getLanguage().name().toLowerCase())
                .build();
    }
}

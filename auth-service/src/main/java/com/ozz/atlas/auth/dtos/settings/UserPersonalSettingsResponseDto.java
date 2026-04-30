package com.ozz.atlas.auth.dtos.settings;

import com.ozz.atlas.auth.domain.UserPersonalSettings;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPersonalSettingsResponseDto {

    private String language;

    public static UserPersonalSettingsResponseDto from(UserPersonalSettings settings) {
        return UserPersonalSettingsResponseDto.builder()
                .language(settings.getLanguage().name().toLowerCase())
                .build();
    }
}

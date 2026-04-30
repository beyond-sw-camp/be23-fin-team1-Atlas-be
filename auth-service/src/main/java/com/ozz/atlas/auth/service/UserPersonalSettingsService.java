package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.UserPersonalSettings;
import com.ozz.atlas.auth.domain.UserPersonalSettingsLanguage;
import com.ozz.atlas.auth.dtos.settings.UserPersonalSettingsResponseDto;
import com.ozz.atlas.auth.dtos.settings.UserPersonalSettingsUpdateDto;
import com.ozz.atlas.auth.repository.UserPersonalSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserPersonalSettingsService {

    private final UserPersonalSettingsRepository userPersonalSettingsRepository;

    @Transactional
    public UserPersonalSettingsResponseDto getSettings(String userPublicId) {
        return UserPersonalSettingsResponseDto.from(getOrCreateSettings(userPublicId));
    }

    @Transactional
    public UserPersonalSettingsResponseDto updateSettings(
            String userPublicId,
            UserPersonalSettingsUpdateDto request
    ) {
        UserPersonalSettings settings = getOrCreateSettings(userPublicId);
        settings.updateLanguage(parseLanguage(request.getLanguage()));
        return UserPersonalSettingsResponseDto.from(settings);
    }

    private UserPersonalSettings getOrCreateSettings(String userPublicId) {
        return userPersonalSettingsRepository.findByUserPublicId(userPublicId)
                .orElseGet(() -> userPersonalSettingsRepository.save(
                        UserPersonalSettings.builder()
                                .userPublicId(userPublicId)
                                .build()
                ));
    }

    private UserPersonalSettingsLanguage parseLanguage(String value) {
        if (value == null) {
            throw invalidLanguage();
        }

        try {
            return UserPersonalSettingsLanguage.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw invalidLanguage();
        }
    }

    private ResponseStatusException invalidLanguage() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 언어 설정입니다.");
    }
}

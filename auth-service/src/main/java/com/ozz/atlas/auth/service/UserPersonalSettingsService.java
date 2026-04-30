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
    public UserPersonalSettingsResponseDto getSettings(Long userId) {
        return UserPersonalSettingsResponseDto.from(getOrCreateSettings(userId));
    }

    @Transactional
    public UserPersonalSettingsResponseDto updateSettings(
            Long userId,
            UserPersonalSettingsUpdateDto request
    ) {
        UserPersonalSettings settings = getOrCreateSettings(userId);
        settings.updateLanguage(parseLanguage(request.getLanguage()));
        return UserPersonalSettingsResponseDto.from(settings);
    }

    private UserPersonalSettings getOrCreateSettings(Long userId) {
        return userPersonalSettingsRepository.findByUserId(userId)
                .orElseGet(() -> userPersonalSettingsRepository.save(
                        UserPersonalSettings.builder()
                                .userId(userId)
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

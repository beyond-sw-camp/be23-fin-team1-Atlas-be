package com.ozz.atlas.control.notification.service;

import com.ozz.atlas.control.notification.domain.NotificationCategory;
import com.ozz.atlas.control.notification.domain.UserNotificationPreference;
import com.ozz.atlas.control.notification.dto.NotificationPreferenceResponse;
import com.ozz.atlas.control.notification.exception.NotificationErrorCode;
import com.ozz.atlas.control.notification.exception.NotificationException;
import com.ozz.atlas.control.notification.repository.UserNotificationPreferenceRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceService {

    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;

    public List<NotificationPreferenceResponse> getPreferences(String userPublicId) {
        Map<NotificationCategory, UserNotificationPreference> preferences =
                userNotificationPreferenceRepository.findAllByUserPublicId(userPublicId).stream()
                        .collect(Collectors.toMap(
                                UserNotificationPreference::getCategory,
                                Function.identity(),
                                (left, right) -> left
                        ));

        return List.of(NotificationCategory.values()).stream()
                .sorted(Comparator.comparingInt(NotificationCategory::getDisplayOrder))
                .map(category -> NotificationPreferenceResponse.of(
                        category,
                        preferences.getOrDefault(category, defaultPreference(userPublicId, category)).isEnabled()
                ))
                .toList();
    }

    @Transactional
    public NotificationPreferenceResponse updatePreference(
            String userPublicId,
            String categoryName,
            boolean enabled
    ) {
        NotificationCategory category = resolveCategory(categoryName);
        UserNotificationPreference preference = userNotificationPreferenceRepository
                .findByUserPublicIdAndCategory(userPublicId, category)
                .orElseGet(() -> userNotificationPreferenceRepository.save(
                        UserNotificationPreference.create(userPublicId, category, true)
                ));

        preference.changeEnabled(enabled);
        return NotificationPreferenceResponse.of(category, preference.isEnabled());
    }

    public List<String> filterEnabledRecipients(String eventType, List<String> recipientUserPublicIds) {
        if (recipientUserPublicIds == null || recipientUserPublicIds.isEmpty()) {
            return List.of();
        }

        return NotificationCategory.fromEventType(eventType)
                .map(category -> filterByCategory(category, recipientUserPublicIds))
                .orElse(recipientUserPublicIds);
    }

    private List<String> filterByCategory(
            NotificationCategory category,
            List<String> recipientUserPublicIds
    ) {
        Set<String> disabledUserPublicIds = userNotificationPreferenceRepository
                .findAllByUserPublicIdInAndCategory(recipientUserPublicIds, category).stream()
                .filter(preference -> !preference.isEnabled())
                .map(UserNotificationPreference::getUserPublicId)
                .collect(Collectors.toCollection(HashSet::new));

        if (disabledUserPublicIds.isEmpty()) {
            return recipientUserPublicIds;
        }

        return recipientUserPublicIds.stream()
                .filter(userPublicId -> !disabledUserPublicIds.contains(userPublicId))
                .toList();
    }

    private NotificationCategory resolveCategory(String categoryName) {
        try {
            return NotificationCategory.valueOf(categoryName);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_CATEGORY_NOT_FOUND);
        }
    }

    private UserNotificationPreference defaultPreference(String userPublicId, NotificationCategory category) {
        return UserNotificationPreference.create(userPublicId, category, true);
    }
}

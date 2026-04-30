package com.ozz.atlas.control.notification.repository;

import com.ozz.atlas.control.notification.domain.NotificationCategory;
import com.ozz.atlas.control.notification.domain.UserNotificationPreference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {

    List<UserNotificationPreference> findAllByUserPublicIdAndCategoryIn(
            String userPublicId,
            Collection<NotificationCategory> categories
    );

    List<UserNotificationPreference> findAllByUserPublicIdInAndCategory(
            Collection<String> userPublicIds,
            NotificationCategory category
    );

    Optional<UserNotificationPreference> findByUserPublicIdAndCategory(
            String userPublicId,
            NotificationCategory category
    );
}

package com.ozz.atlas.control.notification.repository;

import com.ozz.atlas.control.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByPublicId(String publicId);
}

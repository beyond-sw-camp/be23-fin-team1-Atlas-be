package com.ozz.atlas.control.notification.repository;

import com.ozz.atlas.control.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByPublicId(String publicId);
    Page<Notification> findByRecipientUserPublicIdOrderByCreatedAtDesc(String recipientUserPublicId, Pageable pageable);
    long countByRecipientUserPublicIdAndReadYnFalse(String recipientUserPublicId);
    List<Notification> findByRecipientUserPublicIdAndReadYnFalse(String recipientUserPublicId);
}
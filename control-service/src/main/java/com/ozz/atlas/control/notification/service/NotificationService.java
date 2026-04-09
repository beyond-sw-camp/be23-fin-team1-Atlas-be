package com.ozz.atlas.control.notification.service;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.control.config.RedisConstants;
import com.ozz.atlas.control.notification.domain.Notification;
import com.ozz.atlas.control.notification.dto.NotificationDto;
import com.ozz.atlas.control.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 알림을 저장하고 실시간으로 해당 사용자에게 발행합니다.
     */
    @Transactional
    public void saveAndPublish(NotificationDto notificationDto) {
        // 1. 엔티티 저장
        Notification notification = Notification.builder()
                .publicId(PublicIdGenerator.next())
                .recipientUserPublicId(notificationDto.getRecipientUserPublicId())
                .notificationType(notificationDto.getNotificationType())
                .title(notificationDto.getTitle())
                .message(notificationDto.getMessage())
                .deepLinkUrl(notificationDto.getDeepLinkUrl())
                .referencePublicId(notificationDto.getReferencePublicId())
                .build();

        notificationRepository.save(notification);

        // 2. DTO 업데이트
        notificationDto.setPublicId(notification.getPublicId());
        notificationDto.setReadYn(notification.isReadYn());
        notificationDto.setCreatedAt(notification.getCreatedAt());

        // 3. Redis 발행 (notify:user:{userPublicId})
        String topic = RedisConstants.getNotifyUserTopic(notificationDto.getRecipientUserPublicId());
        redisTemplate.convertAndSend(topic, notificationDto);
        
        log.info("Notification published to user: {} (Topic: {})", 
                notificationDto.getRecipientUserPublicId(), topic);
    }

    /**
     * 특정 사용자의 알림 목록 조회 (페이징 지원)
     */
    public Page<NotificationDto> getNotifications(String userPublicId, Pageable pageable) {
        return notificationRepository.findByRecipientUserPublicIdOrderByCreatedAtDesc(userPublicId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(String publicId, String userPublicId) {
        Notification notification = notificationRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getRecipientUserPublicId().equals(userPublicId)) {
            throw new IllegalStateException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
    }

    /**
     * 모든 알림 일괄 읽음 처리
     */
    @Transactional
    public void markAllAsRead(String userPublicId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientUserPublicIdAndReadYnFalse(userPublicId);
        unreadNotifications.forEach(Notification::markAsRead);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public long getUnreadCount(String userPublicId) {
        return notificationRepository.countByRecipientUserPublicIdAndReadYnFalse(userPublicId);
    }

    /**
     * 알림 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteNotification(String publicId, String userPublicId) {
        Notification notification = notificationRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getRecipientUserPublicId().equals(userPublicId)) {
            throw new IllegalStateException("본인의 알림만 삭제할 수 있습니다.");
        }

        notification.delete();
    }

    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .publicId(notification.getPublicId())
                .recipientUserPublicId(notification.getRecipientUserPublicId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .deepLinkUrl(notification.getDeepLinkUrl())
                .readYn(notification.isReadYn())
                .createdAt(notification.getCreatedAt())
                .referencePublicId(notification.getReferencePublicId())
                .build();
    }
}
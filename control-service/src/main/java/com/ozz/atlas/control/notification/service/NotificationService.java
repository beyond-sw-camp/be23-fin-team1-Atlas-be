package com.ozz.atlas.control.notification.service;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.control.config.RedisConstants;
import com.ozz.atlas.control.notification.domain.Notification;
import com.ozz.atlas.control.notification.dto.NotificationDto;
import com.ozz.atlas.control.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

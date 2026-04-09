package com.ozz.atlas.control.notification.controller;

import com.ozz.atlas.control.notification.dto.NotificationDto;
import com.ozz.atlas.control.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @RequestHeader("X-User-Public-Id") String userPublicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(userPublicId, pageable));
    }

    /**
     * 안 읽은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestHeader("X-User-Public-Id") String userPublicId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userPublicId));
    }

    /**
     * 특정 알림 읽음 처리
     */
    @PatchMapping("/{publicId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String publicId,
            @RequestHeader("X-User-Public-Id") String userPublicId) {
        notificationService.markAsRead(publicId, userPublicId);
        return ResponseEntity.ok().build();
    }

    /**
     * 모든 알림 일괄 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Public-Id") String userPublicId) {
        notificationService.markAllAsRead(userPublicId);
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 삭제 (Soft Delete)
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String publicId,
            @RequestHeader("X-User-Public-Id") String userPublicId) {
        notificationService.deleteNotification(publicId, userPublicId);
        return ResponseEntity.noContent().build();
    }
}
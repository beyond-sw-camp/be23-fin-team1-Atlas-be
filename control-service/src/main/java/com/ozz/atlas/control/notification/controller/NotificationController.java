package com.ozz.atlas.control.notification.controller;

import com.ozz.atlas.control.notification.dto.NotificationDto;
import com.ozz.atlas.control.notification.dto.NotificationPreferenceResponse;
import com.ozz.atlas.control.notification.dto.NotificationPreferenceUpdateRequest;
import com.ozz.atlas.control.notification.service.NotificationPreferenceService;
import com.ozz.atlas.control.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;

    /**
     * 내 알림 목록 조회 (페이징)
     */
    @Operation(summary = "내 알림 목록 조회")
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
    @Operation(
            summary = "안 읽은 알림 개수 조회",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(type = "integer", format = "int64", example = "5"),
                            examples = @ExampleObject(value = "5")
                    )
            )
    )
    public ResponseEntity<Long> getUnreadCount(@RequestHeader("X-User-Public-Id") String userPublicId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userPublicId));
    }

    @GetMapping("/preferences")
    @Operation(
            summary = "내 알림 수신 설정 조회",
            description = "사용자가 설정할 수 있는 알림 카테고리 메타데이터와 현재 수신 여부를 조회한다."
    )
    public ResponseEntity<List<NotificationPreferenceResponse>> getMyNotificationPreferences(
            @RequestHeader("X-User-Public-Id") String userPublicId
    ) {
        return ResponseEntity.ok(notificationPreferenceService.getPreferences(userPublicId));
    }

    @PatchMapping("/preferences/{category}")
    @Operation(
            summary = "내 알림 수신 설정 변경",
            description = "카테고리별 개인 알림 수신 여부를 변경한다. 전체 플랫폼 이벤트 ON/OFF는 Kafka 모니터링 규칙에서 관리한다."
    )
    public ResponseEntity<NotificationPreferenceResponse> updateMyNotificationPreference(
            @RequestHeader("X-User-Public-Id") String userPublicId,
            @PathVariable String category,
            @Valid @RequestBody NotificationPreferenceUpdateRequest request
    ) {
        return ResponseEntity.ok(notificationPreferenceService.updatePreference(
                userPublicId,
                category,
                request.enabled()
        ));
    }

    /**
     * 특정 알림 읽음 처리
     */
    @Operation(summary = "알림 읽음 처리")
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
    @Operation(summary = "전체 알림 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Public-Id") String userPublicId) {
        notificationService.markAllAsRead(userPublicId);
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 삭제 (Soft Delete)
     */
    @Operation(summary = "알림 삭제")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String publicId,
            @RequestHeader("X-User-Public-Id") String userPublicId) {
        notificationService.deleteNotification(publicId, userPublicId);
        return ResponseEntity.noContent().build();
    }
}

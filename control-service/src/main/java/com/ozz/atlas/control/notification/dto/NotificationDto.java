package com.ozz.atlas.control.notification.dto;

import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.control.notification.domain.NotificationToastType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "알림 응답")
public class NotificationDto {
    @Schema(description = "알림 공개 식별자", example = "noti_01HZY4NOTI123456789")
    private String publicId;
    @Schema(description = "수신자 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String recipientUserPublicId;
    @Schema(description = "이벤트 타입", example = "inventory.shortage-detected")
    private String eventType;
    @Schema(description = "토스트 표시 타입", example = "WARNING")
    private NotificationToastType notificationType;
    @Schema(description = "알림 도메인 타입", example = "ORDER")
    private DomainType domainType;
    @Schema(description = "알림 제목", example = "새 발주가 접수되었습니다.")
    private String title;
    @Schema(description = "알림 본문", example = "PO-2026-0042 발주가 접수되어 확인이 필요합니다.")
    private String message;
    @Schema(description = "화면 이동용 딥링크", example = "/purchase-orders/po_01HZY4PO123456789")
    private String deepLinkUrl;
    @Schema(description = "연관 리소스 공개 식별자", example = "po_01HZY4PO123456789")
    private String referencePublicId;
    @Schema(description = "읽음 여부", example = "false")
    private boolean readYn;
    @Schema(description = "알림 생성 시각", example = "2026-04-17T09:42:00")
    private LocalDateTime createdAt;
}

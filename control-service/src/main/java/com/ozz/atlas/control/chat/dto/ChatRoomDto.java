package com.ozz.atlas.control.chat.dto;

import com.ozz.atlas.control.chat.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 응답")
public class ChatRoomDto {
    @Schema(description = "채팅방 공개 식별자", example = "room_01HZY4ROOM123456789")
    private String publicId;
    @Schema(description = "채팅방 이름", example = "원재료 공급 이슈 대응")
    private String roomName;
    @Schema(description = "채팅방 상태", example = "ACTIVE")
    private RoomStatus roomStatus;
    @Schema(description = "요청 사용자 또는 소유 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String userAccountPublicId;
    @Schema(description = "채팅방 생성 시각", example = "2026-04-17T10:20:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "안 읽은 메시지 수", example = "3")
    private Long unreadCount;
    @Schema(description = "마지막 메시지", example = "납품 일정 재조정이 필요합니다.")
    private String lastMessage;
    @Schema(description = "마지막 메시지 시각", example = "2026-04-17T10:35:00", nullable = true)
    private LocalDateTime lastMessageAt;

    @Schema(description = "고정된 시각", example = "2026-04-17T10:35:00", nullable = true)
    private LocalDateTime pinnedAt;
}

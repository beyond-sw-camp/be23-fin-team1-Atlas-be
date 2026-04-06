package com.ozz.atlas.control.chat.dto;

import com.ozz.atlas.control.chat.enums.RoomStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    private String publicId;
    private String roomName;
    private RoomStatus roomStatus;
    private String userAccountPublicId;
    private LocalDateTime createdAt;
    
    // 추가된 필드: 안 읽은 메시지 수 및 마지막 메시지
    private Long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}

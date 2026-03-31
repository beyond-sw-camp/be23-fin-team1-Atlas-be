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
}

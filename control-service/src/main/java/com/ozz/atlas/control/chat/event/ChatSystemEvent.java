package com.ozz.atlas.control.chat.event;

import com.ozz.atlas.control.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ChatSystemEvent {
    private final String roomPublicId;
    private final MessageType messageType;
    private final List<String> targetUserPublicIds;
    private final String inviterPublicId; // 초대자의 경우 사용 (선택 사항)
}

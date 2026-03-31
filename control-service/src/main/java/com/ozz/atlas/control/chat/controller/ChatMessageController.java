package com.ozz.atlas.control.chat.controller;

import com.ozz.atlas.control.chat.dto.ChatMessageDto;
import com.ozz.atlas.control.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * WebSocket 메시지 발행 엔드포인트: /pub/chat.message.{roomPublicId}
     */
    @MessageMapping("/chat.message.{roomPublicId}")
    public void message(@DestinationVariable String roomPublicId, ChatMessageDto message) {
        // 경로 변수를 DTO에 설정 (필요 시)
        message.setRoomPublicId(roomPublicId);
        
        // 메시지 저장 및 Redis를 통한 브로드캐스트
        chatMessageService.saveAndPublish(message);
    }

    /**
     * 채팅방 과거 메시지 이력 조회 API
     */
    @GetMapping("/api/v1/chats/rooms/{roomPublicId}/messages")
    public ResponseEntity<Page<ChatMessageDto>> getMessages(
            @PathVariable String roomPublicId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chatMessageService.getMessageHistory(roomPublicId, pageable));
    }

}

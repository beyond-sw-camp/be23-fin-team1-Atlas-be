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
    public void message(
            @DestinationVariable String roomPublicId, 
            ChatMessageDto message, 
            org.springframework.messaging.simp.SimpMessageHeaderAccessor headerAccessor
    ) {
        // 경로 변수를 DTO에 설정 (필요 시)
        message.setRoomPublicId(roomPublicId);
        
        // 왜(Why): 클라이언트가 전달한 발신자 ID는 위/변조의 위험(사칭)이 있으므로 신뢰하지 않는다.
        // 따라서 인터셉터(StompAuthInterceptor)에서 JWT 검증 후 세션에 저장한 안전한 userPublicId를 발신자로 강제 설정한다.
        java.util.Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("userPublicId")) {
            message.setSenderUserPublicId((String) sessionAttributes.get("userPublicId"));
        }
        
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

package com.ozz.atlas.control.chat.controller;

import com.ozz.atlas.control.chat.dto.ChatMessageDto;
import com.ozz.atlas.control.chat.service.ChatMessageService;
import com.ozz.atlas.control.chat.search.service.ChatMessageSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@Tag(name = "ChatMessage")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatMessageSearchService chatMessageSearchService;


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
     * 채팅방 과거 메시지 이력 조회 API (커서 기반 페이지네이션 지원)
     * cursor 파라미터가 없으면 가장 최신 메시지부터, 있으면 해당 cursor(publicId) 이전 메시지를 반환
     */
    @Operation(summary = "채팅 메시지 목록 조회")
    @GetMapping("/api/control/chats/rooms/{roomPublicId}/messages")
    public ResponseEntity<Page<ChatMessageDto>> getMessages(
            @PathVariable String roomPublicId,
            @RequestParam(required = false) String cursor,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(chatMessageService.getMessageHistory(roomPublicId, cursor, pageable));
    }

    /**
     * 특정 채팅방 안의 메시지를 키워드로 검색한다.
     * 메시지 본문, 참조 코드, 참조 제목 기준으로 부분검색이 가능하다.
     */
    @Operation(summary = "채팅 메시지 검색")
    @GetMapping("/api/control/chats/rooms/{roomPublicId}/messages/search")
    public ResponseEntity<Page<ChatMessageDto>> searchMessages(
            @PathVariable String roomPublicId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(chatMessageSearchService.search(roomPublicId, keyword, pageable));
    }


    @Operation(summary = "채팅 메시지 수정")
    @PutMapping("/api/control/chats/messages/{messagePublicId}")
    public ResponseEntity<ChatMessageDto> updateMessage(
            @PathVariable String messagePublicId,
            @RequestBody com.ozz.atlas.control.chat.dto.UpdateMessageRequestDto request,
            @RequestHeader("X-User-Public-Id") String userPublicId) {
        return ResponseEntity.ok(chatMessageService.updateMessage(messagePublicId, request.getMessageBody(), userPublicId));
    }

    @Operation(summary = "채팅 메시지 삭제")
    @DeleteMapping("/api/control/chats/messages/{messagePublicId}")
    public ResponseEntity<ChatMessageDto> deleteMessage(
            @PathVariable String messagePublicId,
            @RequestHeader("X-User-Public-Id") String userPublicId) {
        return ResponseEntity.ok(chatMessageService.deleteMessage(messagePublicId, userPublicId));
    }
}

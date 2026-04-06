package com.ozz.atlas.control.chat.controller;

import com.ozz.atlas.control.chat.dto.ChatRoomDto;
import com.ozz.atlas.control.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chats/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성
     * Request Body: { "roomName": "...", "creatorPublicId": "...", "participantIds": ["...", "..."] }
     */
    @PostMapping
    public ResponseEntity<ChatRoomDto> createRoom(@RequestBody Map<String, Object> request) {
        String roomName = (String) request.get("roomName");
        String creatorPublicId = (String) request.get("creatorPublicId");
        List<String> participantIds = (List<String>) request.get("participantIds");

        return ResponseEntity.ok(chatRoomService.createRoom(roomName, creatorPublicId, participantIds));
    }

    /**
     * 사용자가 속한 채팅방 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> getRooms(@RequestParam String userPublicId) {
        return ResponseEntity.ok(chatRoomService.findAllRoomsByUser(userPublicId));
    }

    /**
     * 채팅방 메시지 읽음 처리
     */
    @PostMapping("/{roomPublicId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String roomPublicId, @RequestParam String userPublicId) {
        chatRoomService.markAsRead(roomPublicId, userPublicId);
        return ResponseEntity.ok().build();
    }
}

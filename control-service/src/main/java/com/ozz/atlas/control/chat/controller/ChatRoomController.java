package com.ozz.atlas.control.chat.controller;

import com.ozz.atlas.control.chat.dto.ChatRoomDto;
import com.ozz.atlas.control.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ozz.atlas.control.chat.search.document.ChatParticipantDocument;
import com.ozz.atlas.control.chat.search.service.ChatParticipantSearchService;
import com.ozz.atlas.control.chat.search.service.ChatRoomSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/control/chats/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatParticipantSearchService chatParticipantSearchService;
    private final ChatRoomSearchService chatRoomSearchService;



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
    public ResponseEntity<Page<ChatRoomDto>> getRooms(
            @RequestParam String userPublicId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.ok(chatRoomService.findAllRoomsByUser(userPublicId, pageable));
        }
        return ResponseEntity.ok(chatRoomSearchService.search(userPublicId, keyword, pageable));
    }

    /**
     * 채팅방 메시지 읽음 처리 (수동)
     */
    @PatchMapping("/{roomPublicId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String roomPublicId, 
            @RequestBody(required = false) com.ozz.atlas.control.chat.dto.MarkAsReadRequestDto request,
            @RequestHeader("X-User-Public-Id") String userPublicId) {
        
        String messagePublicId = (request != null) ? request.getLastReadMessagePublicId() : null;
        chatRoomService.markAsRead(roomPublicId, userPublicId, messagePublicId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 참가자 초대
     * Request Body: { "inviterPublicId": "...", "targetUserPublicIds": ["...", "..."] }
     */
    @PostMapping("/{roomPublicId}/participants")
    public ResponseEntity<Void> inviteParticipants(
            @PathVariable String roomPublicId, 
            @RequestBody com.ozz.atlas.control.chat.dto.InviteParticipantsDto request) {
        String inviterPublicId = request.getInviterPublicId();
        List<String> targetUserPublicIds = request.getTargetUserPublicIds();
        
        chatRoomService.inviteParticipants(roomPublicId, inviterPublicId, targetUserPublicIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 나가기
     */
    @DeleteMapping("/{roomPublicId}/participants")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable String roomPublicId, 
            @RequestParam String userPublicId) {
        chatRoomService.leaveRoom(roomPublicId, userPublicId);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 채팅방 안의 참여자를 검색
     * 이름, 로그인아이디, 이메일 기준으로 부분검색이 가능
     * GET /api/control/chats/rooms/{roomPublicId}/participants/search?keyword=홍
     * GET /api/control/chats/rooms/{roomPublicId}/participants/search?keyword=manager
     */
    @GetMapping("/{roomPublicId}/participants/search")
    public ResponseEntity<Page<ChatParticipantDocument>> searchParticipants(
            @PathVariable String roomPublicId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ChatParticipantDocument> response =
                chatParticipantSearchService.search(roomPublicId, keyword, pageable);

        return ResponseEntity.ok(response);
    }

}

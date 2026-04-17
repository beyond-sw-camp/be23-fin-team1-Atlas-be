package com.ozz.atlas.control.chat.controller;

import com.ozz.atlas.control.chat.dto.ChatRoomDto;
import com.ozz.atlas.control.chat.dto.InviteParticipantsDto;
import com.ozz.atlas.control.chat.dto.MarkAsReadRequestDto;
import com.ozz.atlas.control.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "ChatRoom")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatParticipantSearchService chatParticipantSearchService;
    private final ChatRoomSearchService chatRoomSearchService;



    /**
     * 채팅방 생성
     * Request Body: { "roomName": "...", "creatorPublicId": "...", "participantIds": ["...", "..."] }
     */
    @PostMapping
    @Operation(
            summary = "채팅방 생성",
            description = "참여자 목록을 포함한 신규 채팅방을 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "roomName": "원재료 공급 이슈 대응",
                                              "creatorPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "participantIds": [
                                                "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                                "usr_01HZY4U2"
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ChatRoomDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "publicId": "room_01HZY4ROOM123456789",
                                              "roomName": "원재료 공급 이슈 대응",
                                              "roomStatus": "ACTIVE",
                                              "userAccountPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "createdAt": "2026-04-17T10:20:00",
                                              "unreadCount": 0,
                                              "lastMessage": null,
                                              "lastMessageAt": null
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ChatRoomDto> createRoom(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(
                chatRoomService.createRoom(
                        (String) request.get("roomName"),
                        (String) request.get("creatorPublicId"),
                        (List<String>) request.get("participantIds")
                )
        );
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
    @Operation(
            summary = "채팅 읽음 처리",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    content = @Content(
                            schema = @Schema(implementation = MarkAsReadRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "lastReadMessagePublicId": "msg_01HZY4MSG123456789"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<Void> markAsRead(
            @PathVariable String roomPublicId, 
            @RequestBody(required = false) MarkAsReadRequestDto request,
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
    @Operation(
            summary = "채팅방 참여자 초대",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = InviteParticipantsDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "inviterPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "targetUserPublicIds": ["usr_01HZY4U2", "usr_01HZY4U3"]
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<Void> inviteParticipants(
            @PathVariable String roomPublicId, 
            @RequestBody InviteParticipantsDto request) {
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

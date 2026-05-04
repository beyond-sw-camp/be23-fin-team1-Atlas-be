package com.ozz.atlas.control.chat.service;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import com.ozz.atlas.control.chat.domain.ChatRoomPin;
import com.ozz.atlas.control.chat.dto.ChatRoomDto;
import com.ozz.atlas.control.chat.enums.MessageType;
import com.ozz.atlas.control.chat.enums.RoomStatus;
import com.ozz.atlas.control.chat.event.ChatSystemEvent;
import com.ozz.atlas.control.chat.exception.ChatErrorCode;
import com.ozz.atlas.control.chat.exception.ChatException;
import com.ozz.atlas.control.chat.repository.ChatMessageRepository;
import com.ozz.atlas.control.chat.repository.ChatParticipantRepository;
import com.ozz.atlas.control.chat.repository.ChatRoomRepository;
import com.ozz.atlas.control.chat.repository.ChatRoomPinRepository;
import com.ozz.atlas.control.chat.search.service.ChatParticipantSearchService;
import com.ozz.atlas.control.chat.search.service.ChatRoomSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomPinRepository chatRoomPinRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatParticipantSearchService chatParticipantSearchService;
    private final ChatRoomSearchService chatRoomSearchService;

    @Transactional
    public Map<String, Object> pinRoom(String roomPublicId, String userPublicId) {
        ChatRoom chatRoom = chatRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatParticipant participant = chatParticipantRepository.findByChatRoomAndUserPublicId(chatRoom, userPublicId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND)); // Assuming PARTICIPANT_NOT_FOUND might not exist, fallback to existing error or add if possible. Wait, the error was "cannot find symbol: variable PARTICIPANT_NOT_FOUND location: class ChatErrorCode". Let's check ChatErrorCode.
        
        if (!participant.isActiveYn()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        ChatRoomPin pin = chatRoomPinRepository.findByChatRoomAndUserPublicId(chatRoom, userPublicId)
                .orElseGet(() -> {
                    ChatRoomPin newPin = ChatRoomPin.builder()
                            .chatRoom(chatRoom)
                            .userPublicId(userPublicId)
                            .build();
                    return chatRoomPinRepository.save(newPin);
                });

        return Map.of("pinnedAt", pin.getPinnedAt().toString());
    }

    @Transactional
    public void unpinRoom(String roomPublicId, String userPublicId) {
        ChatRoom chatRoom = chatRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        chatRoomPinRepository.deleteByChatRoomAndUserPublicId(chatRoom, userPublicId);
    }



    /**
     * 채팅방 생성
     */
    @Transactional
    public ChatRoomDto createRoom(String roomName, String creatorPublicId, List<String> participantIds) {
        // 1. 채팅방 엔티티 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .publicId(PublicIdGenerator.next())
                .roomName(roomName)
                .roomStatus(RoomStatus.OPEN)
                .userAccountPublicId(creatorPublicId)
                .build();
        
        chatRoomRepository.save(chatRoom);

        // 2. 참여자 추가 (생성자 포함)
        addParticipant(chatRoom, creatorPublicId, "OWNER");
        for (String participantId : participantIds) {
            if (!participantId.equals(creatorPublicId)) {
                addParticipant(chatRoom, participantId, "MEMBER");
            }
        }
        // 참여자까지 모두 추가된 뒤 채팅방 검색 문서를 저장
        chatRoomSearchService.saveChatRoomDocument(chatRoom);

        return convertToDto(chatRoom, creatorPublicId);
    }

    private void addParticipant(ChatRoom chatRoom, String userPublicId, String role) {
        // 이미 나갔던 사람이 다시 들어오는지 확인
        chatParticipantRepository.findByChatRoomAndUserPublicId(chatRoom, userPublicId).ifPresentOrElse(
            participant -> {
                if (!participant.isActiveYn()) {
                    participant.activateFrom(LocalDateTime.now());
                    chatParticipantSearchService.saveChatParticipantDocument(participant);
                }
            },
            () -> {
                ChatParticipant newParticipant = ChatParticipant.builder()
                        .chatRoom(chatRoom)
                        .userPublicId(userPublicId)
                        .participantRole(role)
                        .build();
                chatParticipantRepository.save(newParticipant);
                // 참여자가 새로 추가되면 ES 참여자 검색 문서도 함께 저장
                // 이때 auth-service 에서 사용자 이름, 로그인아이디, 이메일을 조회해 색인
                chatParticipantSearchService.saveChatParticipantDocument(newParticipant);
            }
        );
    }

    /**
     * 사용자가 속한 채팅방 목록 조회 (활성 참여자만)
     */
    public Page<ChatRoomDto> findAllRoomsByUser(String userPublicId, Pageable pageable) {
        return chatParticipantRepository.findByUserPublicIdActive(userPublicId, pageable)
                .map(participant -> {
                    ChatRoom chatRoom = participant.getChatRoom();
                    ChatRoomDto dto = convertToDto(chatRoom, userPublicId);
                    
                    // 안 읽은 메시지 수 계산
                    Long unreadCount = chatMessageRepository.countVisibleUnreadMessages(
                            chatRoom,
                            participant.getLastReadMessageId(),
                            participant.getVisibleFromAt()
                    );
                    dto.setUnreadCount(unreadCount);
                    
                    // 마지막 메시지 조회
                    chatMessageRepository.findLatestVisibleByChatRoom(chatRoom, participant.getVisibleFromAt(), PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .ifPresent(lastMessage -> {
                        dto.setLastMessage(lastMessage.getMessageBody());
                        dto.setLastMessageAt(lastMessage.getCreatedAt());
                    });
                    
                    return dto;
                });

    }

    /**
     * 특정 채팅방 조회 (public_id 기반)
     */
    public ChatRoom findRoomByPublicId(String roomPublicId) {
        return chatRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new com.ozz.atlas.control.chat.exception.ChatException(com.ozz.atlas.control.chat.exception.ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    /**
     * 채팅방 이름 변경
     */
    @Transactional
    public ChatRoomDto updateRoomName(String roomPublicId, String newRoomName, String userPublicId) {
        ChatRoom chatRoom = findRoomByPublicId(roomPublicId);
        
        chatRoom.updateRoomName(newRoomName);
        chatRoomSearchService.saveChatRoomDocument(chatRoom);
        
        return ChatRoomDto.builder()
                .publicId(chatRoom.getPublicId())
                .roomName(chatRoom.getRoomName())
                .roomStatus(chatRoom.getRoomStatus())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    /**
     * 채팅방 읽음 처리
     */
    @Transactional
    public void markAsRead(String roomPublicId, String userPublicId, String messagePublicId) {
        ChatRoom chatRoom = findRoomByPublicId(roomPublicId);
        
        if (messagePublicId != null && !messagePublicId.isBlank()) {
            chatMessageRepository.findByPublicId(messagePublicId).ifPresent(message -> {
                chatParticipantRepository.updateLastReadMessageIdForUsers(chatRoom, List.of(userPublicId), message.getId());
            });
        } else {
            chatParticipantRepository.findByChatRoomAndUserPublicIdActive(chatRoom, userPublicId)
                    .flatMap(participant -> chatMessageRepository
                            .findLatestVisibleByChatRoom(chatRoom, participant.getVisibleFromAt(), PageRequest.of(0, 1))
                            .stream()
                            .findFirst())
                    .ifPresent(lastMessage -> {
                        chatParticipantRepository.updateLastReadMessageIdForUsers(chatRoom, List.of(userPublicId), lastMessage.getId());
                    });
        }
    }

    /**
     * 채팅방 참가자 초대
     */
    @Transactional
    public void inviteParticipants(String roomPublicId, String inviterPublicId, List<String> targetUserPublicIds) {
        ChatRoom chatRoom = findRoomByPublicId(roomPublicId);
        
        for (String targetId : targetUserPublicIds) {
            addParticipant(chatRoom, targetId, "MEMBER");
        }

        // 시스템 이벤트 발행 (입장 메시지용)
        eventPublisher.publishEvent(ChatSystemEvent.builder()
                .roomPublicId(roomPublicId)
                .messageType(MessageType.SYSTEM_JOIN)
                .targetUserPublicIds(targetUserPublicIds)
                .inviterPublicId(inviterPublicId)
                .build());
        chatRoomSearchService.saveChatRoomDocument(chatRoom);
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public void leaveRoom(String roomPublicId, String targetUserPublicId) {
        ChatRoom chatRoom = findRoomByPublicId(roomPublicId);
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean directRoom = participants.size() == 2;
        
        // 해당 유저를 비활성(soft delete) 처리
        chatParticipantRepository.findByChatRoomAndUserPublicId(chatRoom, targetUserPublicId)
                .ifPresent(participant -> {
                    participant.deactivate();
                    chatParticipantSearchService.saveChatParticipantDocument(participant);
                });

        if (!directRoom) {
            // 시스템 이벤트 발행 (퇴장 메시지용)
            eventPublisher.publishEvent(ChatSystemEvent.builder()
                    .roomPublicId(roomPublicId)
                    .messageType(MessageType.SYSTEM_LEAVE)
                    .targetUserPublicIds(List.of(targetUserPublicId))
                    .build());
        }
        chatRoomSearchService.saveChatRoomDocument(chatRoom);
    }

    private ChatRoomDto convertToDto(ChatRoom chatRoom, String userPublicId) {
        LocalDateTime pinnedAt = chatRoomPinRepository.findByChatRoomAndUserPublicId(chatRoom, userPublicId)
                .map(ChatRoomPin::getPinnedAt)
                .orElse(null);

        List<com.ozz.atlas.control.chat.dto.ChatParticipantDto> participantDtos = chatParticipantSearchService.getParticipantsByRoomPublicId(chatRoom.getPublicId())
                .stream()
                .map(doc -> com.ozz.atlas.control.chat.dto.ChatParticipantDto.builder()
                        .userPublicId(doc.getUserPublicId())
                        .displayName(doc.getDisplayName())
                        .profileImageThumbPath(doc.getProfileImageThumbPath())
                        .build())
                .toList();

        return ChatRoomDto.builder()
                .publicId(chatRoom.getPublicId())
                .roomName(chatRoom.getRoomName())
                .roomStatus(chatRoom.getRoomStatus())
                .userAccountPublicId(chatRoom.getUserAccountPublicId())
                .createdAt(chatRoom.getCreatedAt())
                .pinnedAt(pinnedAt)
                .participants(participantDtos)
                .build();
    }

    //     1:1 방이 있으면 그 방 반환, 없으면 새로 생성
    @Transactional
    public ChatRoomDto findOrCreateDirectRoom(
            String roomName,
            String creatorPublicId,
            String targetUserPublicId
    ) {
        if (creatorPublicId == null || targetUserPublicId == null) {
            throw new IllegalArgumentException("1:1 채팅 사용자 정보가 없습니다.");
        }

        if (creatorPublicId.equals(targetUserPublicId)) {
            throw new IllegalArgumentException("자기 자신과는 1:1 채팅을 시작할 수 없습니다.");
        }

        // 활성 여부와 무관하게 두 사용자로만 구성된 기존 1:1 방을 먼저 찾습니다.
        for (ChatParticipant myParticipant : chatParticipantRepository.findByUserPublicId(creatorPublicId)) {
            ChatRoom room = myParticipant.getChatRoom();

            List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(room);
            List<String> participantIds = participants
                    .stream()
                    .map(ChatParticipant::getUserPublicId)
                    .toList();

            boolean isSameDirectRoom =
                    participants.size() == 2
                            && participantIds.contains(creatorPublicId)
                            && participantIds.contains(targetUserPublicId);

            // 이미 정확한 1:1 방이 있으면 새로 만들지 않고 기존 방을 재활성화해서 돌려줌
            if (isSameDirectRoom) {
                participants.stream()
                        .filter(participant -> participant.getUserPublicId().equals(creatorPublicId))
                        .findFirst()
                        .ifPresent(participant -> {
                            if (!participant.isActiveYn()) {
                                participant.activateFrom(LocalDateTime.now());
                                chatParticipantSearchService.saveChatParticipantDocument(participant);
                            }
                        });
                chatRoomSearchService.saveChatRoomDocument(room);
                return convertToDto(room, creatorPublicId);
            }
        }

        // 정확한 1:1 방이 없으면 새로 만듭니다.
        return createRoom(roomName, creatorPublicId, List.of(targetUserPublicId));
    }

}

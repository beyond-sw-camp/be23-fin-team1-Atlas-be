package com.ozz.atlas.control.chat.service;

import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.control.config.RedisConstants;
import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import com.ozz.atlas.control.chat.dto.ChatMessageDto;
import com.ozz.atlas.control.chat.event.ChatSystemEvent;
import com.ozz.atlas.control.chat.repository.ChatMessageRepository;
import com.ozz.atlas.control.chat.repository.ChatParticipantRepository;
import com.ozz.atlas.control.notification.dto.NotificationDto;
import com.ozz.atlas.control.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;
    private final ChatPresenceService chatPresenceService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 메시지 저장 및 Redis 발행 (동적 토픽 및 참여자 알림 포함)
     */
    @Transactional
    public void saveAndPublish(ChatMessageDto messageDto) {
        // 1. 채팅방 확인
        ChatRoom chatRoom = chatRoomService.findRoomByPublicId(messageDto.getRoomPublicId());

        // 2. 메시지 엔티티 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .publicId(PublicIdGenerator.next())
                .chatRoom(chatRoom)
                .senderUserPublicId(messageDto.getSenderUserPublicId())
                .messageType(messageDto.getMessageType())
                .messageBody(messageDto.getMessageBody())
                .referenceType(messageDto.getReferenceType())
                .referencePublicId(messageDto.getReferencePublicId())
                .referenceCode(messageDto.getReferenceCode())
                .referenceTitle(messageDto.getReferenceTitle())
                .build();
        
        chatMessageRepository.save(chatMessage);

        // 3. 현재 방을 보고 있는 유저들(Presence) 실시간 읽음 처리 (발신자 포함)
        Set<String> viewingUsers = chatPresenceService.getViewingUsers(chatRoom.getPublicId());
        List<String> readUserIds = new ArrayList<>(viewingUsers);
        
        if (StringUtils.hasText(messageDto.getSenderUserPublicId()) && !readUserIds.contains(messageDto.getSenderUserPublicId())) {
            readUserIds.add(messageDto.getSenderUserPublicId()); // 발신자는 항상 읽은 것으로 처리 (시스템 메시지는 발신자 null 가능)
        }
        chatParticipantRepository.updateLastReadMessageIdForUsers(chatRoom, readUserIds, chatMessage.getId());

        // 4. DTO 업데이트
        messageDto.setPublicId(chatMessage.getPublicId());
        messageDto.setSentAt(chatMessage.getCreatedAt());

        // 5. Redis 발행 (방별 동적 토픽: chat:room:{public_id})
        String topic = RedisConstants.getChatRoomTopic(chatRoom.getPublicId());
        redisTemplate.convertAndSend(topic, messageDto);

        // 6. 참여자들에게 알림 발송 (현재 방을 보고 있지 않은 참여자에게만)
        // 시스템 메시지는 푸시 알림 생략
        if (messageDto.getMessageType().name().startsWith("SYSTEM")) {
            return;
        }
        sendChatNotifications(chatRoom, messageDto, viewingUsers);
    }

    private void sendChatNotifications(ChatRoom chatRoom, ChatMessageDto messageDto, Set<String> viewingUsers) {
        chatParticipantRepository.findByChatRoomActive(chatRoom).stream()
                .filter(p -> !p.getUserPublicId().equals(messageDto.getSenderUserPublicId()))
                .filter(p -> !viewingUsers.contains(p.getUserPublicId())) // 보고 있는 사람은 푸시 알림 제외
                .forEach(participant -> {
                    NotificationDto notification = NotificationDto.builder()
                            .recipientUserPublicId(participant.getUserPublicId())
                            .notificationType(DomainType.CHAT)
                            .title(chatRoom.getRoomName() + " 방에 새 메시지가 도착했습니다.")
                            .message(messageDto.getMessageBody())
                            .deepLinkUrl("/chats/rooms/" + chatRoom.getPublicId())
                            .referencePublicId(chatRoom.getPublicId())
                            .build();
                    notificationService.saveAndPublish(notification);
                });
    }

    /**
     * 시스템 메시지 이벤트 리스너 (참여자 입장/퇴장 시)
     */
    @Transactional
    @EventListener
    public void handleChatSystemEvent(ChatSystemEvent event) {
        String userNames = String.join(", ", event.getTargetUserPublicIds()); // 실제로는 UserService를 통해 이름 변환이 필요함
        String messageBody = "";

        switch (event.getMessageType()) {
            case SYSTEM_JOIN:
                messageBody = userNames + " 님이 채팅방에 입장했습니다.";
                break;
            case SYSTEM_LEAVE:
                messageBody = userNames + " 님이 채팅방에서 퇴장했습니다.";
                break;
            default:
                messageBody = "시스템 알림입니다.";
        }

        ChatMessageDto systemMessage = ChatMessageDto.builder()
                .roomPublicId(event.getRoomPublicId())
                .messageType(event.getMessageType())
                .messageBody(messageBody)
                // senderUserPublicId 는 null 로 두어 시스템 메시지임을 식별
                .build();

        saveAndPublish(systemMessage);
    }

    /**
     * 채팅방 이력 조회 (페이지네이션) - cursor(public_id) 기반
     */
    public Page<ChatMessageDto> getMessageHistory(String roomPublicId, String cursor, Pageable pageable) {
        ChatRoom chatRoom = chatRoomService.findRoomByPublicId(roomPublicId);
        
        if (StringUtils.hasText(cursor)) {
            // 커서가 존재할 경우: 커서 메시지의 PK 조회 후 그 이전 메시지들 반환
            return chatMessageRepository.findByPublicId(cursor)
                    .map(message -> chatMessageRepository.findByChatRoomAndIdLessThanOrderByIdDesc(chatRoom, message.getId(), pageable))
                    .orElseGet(() -> chatMessageRepository.findByChatRoomOrderByIdDesc(chatRoom, pageable))
                    .map(this::convertToDto);
        }
        
        // 커서가 없는 경우: 가장 최신 메시지부터 반환
        return chatMessageRepository.findByChatRoomOrderByIdDesc(chatRoom, pageable)
                .map(this::convertToDto);
    }

    private ChatMessageDto convertToDto(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .publicId(chatMessage.getPublicId())
                .roomPublicId(chatMessage.getChatRoom().getPublicId())
                .senderUserPublicId(chatMessage.getSenderUserPublicId())
                .messageType(chatMessage.getMessageType())
                .messageBody(chatMessage.getMessageBody())
                .referenceType(chatMessage.getReferenceType())
                .referencePublicId(chatMessage.getReferencePublicId())
                .referenceCode(chatMessage.getReferenceCode())
                .referenceTitle(chatMessage.getReferenceTitle())
                .sentAt(chatMessage.getCreatedAt())
                .build();
    }
}

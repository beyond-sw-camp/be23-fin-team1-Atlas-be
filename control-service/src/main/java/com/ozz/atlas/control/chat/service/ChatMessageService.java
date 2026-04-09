package com.ozz.atlas.control.chat.service;

import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.control.config.RedisConstants;
import com.ozz.atlas.control.client.SupplyServiceClient;
import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import com.ozz.atlas.control.chat.dto.ChatMessageDto;
import com.ozz.atlas.control.chat.event.ChatSystemEvent;
import com.ozz.atlas.control.chat.repository.ChatMessageRepository;
import com.ozz.atlas.control.chat.repository.ChatParticipantRepository;
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
    private final ChatPresenceService chatPresenceService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SupplyServiceClient supplyServiceClient;

    /**
     * 메시지 저장 및 Redis 발행 (동적 토픽 및 참여자 알림 포함)
     */
    @Transactional
    public void saveAndPublish(ChatMessageDto messageDto) {
        // 참조 데이터 자동 검증 (무결성 보장 로직)
        if (messageDto.getReferenceType() != null && StringUtils.hasText(messageDto.getReferencePublicId())) {
            boolean isValid = true;
            switch (messageDto.getReferenceType()) {
                case RETURN_REQUEST:
                    isValid = supplyServiceClient.validateReturnRequest(messageDto.getReferencePublicId());
                    break;
                case ORDER:
                    isValid = supplyServiceClient.validatePurchaseOrder(messageDto.getReferencePublicId());
                    break;
                case ITEM:
                    isValid = supplyServiceClient.validateItem(messageDto.getReferencePublicId());
                    break;
                // 기타 도메인은 생략 또는 기본 통과 처리
            }
            if (!isValid) {
                throw new IllegalArgumentException("유효하지 않은 참조 데이터입니다. (Type: " + messageDto.getReferenceType() + ", ID: " + messageDto.getReferencePublicId() + ")");
            }
        }

        // 1. 채팅방 확인
        ChatRoom chatRoom = chatRoomService.findRoomByPublicId(messageDto.getRoomPublicId());

        // 2. 메시지 엔티티 저장
        String attachments = messageDto.getAttachmentPublicIds() != null && !messageDto.getAttachmentPublicIds().isEmpty() 
                ? String.join(",", messageDto.getAttachmentPublicIds()) 
                : null;

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
                .attachmentPublicIds(attachments)
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

    @Transactional
    public ChatMessageDto updateMessage(String messagePublicId, String newBody, String actorPublicId) {
        ChatMessage chatMessage = chatMessageRepository.findByPublicId(messagePublicId)
                .orElseThrow(() -> new com.ozz.atlas.control.chat.exception.ChatException(com.ozz.atlas.control.chat.exception.ChatErrorCode.CHAT_MESSAGE_NOT_FOUND));

        if (!chatMessage.getSenderUserPublicId().equals(actorPublicId)) {
            throw new com.ozz.atlas.control.chat.exception.ChatException(com.ozz.atlas.control.chat.exception.ChatErrorCode.NOT_MESSAGE_SENDER);
        }
        if (chatMessage.getStatus() == com.ozz.atlas.common.jpa.Status.DELETE) {
            throw new com.ozz.atlas.control.chat.exception.ChatException(com.ozz.atlas.control.chat.exception.ChatErrorCode.MESSAGE_ALREADY_DELETED);
        }

        chatMessage.updateMessage(newBody);
        ChatMessageDto dto = convertToDto(chatMessage);
        
        // 수정된 정보 브로드캐스트
        String topic = RedisConstants.getChatRoomTopic(chatMessage.getChatRoom().getPublicId());
        redisTemplate.convertAndSend(topic, dto);
        
        return dto;
    }

    @Transactional
    public ChatMessageDto deleteMessage(String messagePublicId, String actorPublicId) {
        ChatMessage chatMessage = chatMessageRepository.findByPublicId(messagePublicId)
                .orElseThrow(() -> new com.ozz.atlas.control.chat.exception.ChatException(com.ozz.atlas.control.chat.exception.ChatErrorCode.CHAT_MESSAGE_NOT_FOUND));

        if (!chatMessage.getSenderUserPublicId().equals(actorPublicId)) {
            throw new com.ozz.atlas.control.chat.exception.ChatException(com.ozz.atlas.control.chat.exception.ChatErrorCode.NOT_MESSAGE_SENDER);
        }
        
        chatMessage.delete();
        ChatMessageDto dto = convertToDto(chatMessage);

        // 삭제된 정보 브로드캐스트
        String topic = RedisConstants.getChatRoomTopic(chatMessage.getChatRoom().getPublicId());
        redisTemplate.convertAndSend(topic, dto);

        return dto;
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
        List<String> attachments = (chatMessage.getAttachmentPublicIds() != null && !chatMessage.getAttachmentPublicIds().isBlank())
                ? java.util.Arrays.asList(chatMessage.getAttachmentPublicIds().split(","))
                : java.util.Collections.emptyList();

        boolean isDeleted = chatMessage.getStatus() == com.ozz.atlas.common.jpa.Status.DELETE;
        String displayBody = isDeleted ? "[삭제된 메시지입니다.]" : chatMessage.getMessageBody();
        
        // Hide attachments if deleted
        if (isDeleted) {
            attachments = java.util.Collections.emptyList();
        }

        return ChatMessageDto.builder()
                .publicId(chatMessage.getPublicId())
                .roomPublicId(chatMessage.getChatRoom().getPublicId())
                .senderUserPublicId(chatMessage.getSenderUserPublicId())
                .messageType(chatMessage.getMessageType())
                .messageBody(displayBody)
                .referenceType(chatMessage.getReferenceType())
                .referencePublicId(chatMessage.getReferencePublicId())
                .referenceCode(chatMessage.getReferenceCode())
                .referenceTitle(chatMessage.getReferenceTitle())
                .attachmentPublicIds(attachments)
                .sentAt(chatMessage.getCreatedAt())
                .editedAt(chatMessage.getEditedAt())
                .isDeleted(isDeleted)
                .build();
    }
}
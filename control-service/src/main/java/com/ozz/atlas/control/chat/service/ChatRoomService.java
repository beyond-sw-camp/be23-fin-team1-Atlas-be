package com.ozz.atlas.control.chat.service;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import com.ozz.atlas.control.chat.dto.ChatRoomDto;
import com.ozz.atlas.control.chat.enums.RoomStatus;
import com.ozz.atlas.control.chat.repository.ChatParticipantRepository;
import com.ozz.atlas.control.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

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

        return convertToDto(chatRoom);
    }

    private void addParticipant(ChatRoom chatRoom, String userPublicId, String role) {
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .userPublicId(userPublicId)
                .participantRole(role)
                .build();
        chatParticipantRepository.save(participant);
    }

    /**
     * 사용자가 속한 채팅방 목록 조회
     */
    public List<ChatRoomDto> findAllRoomsByUser(String userPublicId) {
        return chatParticipantRepository.findByUserPublicId(userPublicId).stream()
                .map(participant -> convertToDto(participant.getChatRoom()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅방 조회 (public_id 기반)
     */
    public ChatRoom findRoomByPublicId(String roomPublicId) {
        return chatRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomPublicId));
    }

    private ChatRoomDto convertToDto(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .publicId(chatRoom.getPublicId())
                .roomName(chatRoom.getRoomName())
                .roomStatus(chatRoom.getRoomStatus())
                .userAccountPublicId(chatRoom.getUserAccountPublicId())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}

package com.ozz.atlas.control.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.control.config.RedisConstants;
import com.ozz.atlas.control.chat.dto.ChatMessageDto;
import com.ozz.atlas.control.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    @Qualifier("chatRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Redis에서 메시지가 발행(publish)되면 수신하여 STOMP로 브로드캐스팅합니다.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String topic = new String(message.getChannel(), StandardCharsets.UTF_8);
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());

            log.debug("Received message from topic: {}", topic);

            // 1. 채팅방 메시지인 경우 (chat:room:{public_id})
            if (topic.startsWith(RedisConstants.CHAT_ROOM_TOPIC_PREFIX)) {
                ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);
                messagingTemplate.convertAndSend("/sub/chat.room." + chatMessage.getRoomPublicId(), chatMessage);
            } 
            
            // 2. 알림 메시지인 경우 (notify:user:{public_id})
            else if (topic.startsWith(RedisConstants.NOTIFY_USER_TOPIC_PREFIX)) {
                NotificationDto notification = objectMapper.readValue(publishMessage, NotificationDto.class);
                messagingTemplate.convertAndSend("/sub/notify.user." + notification.getRecipientUserPublicId(), notification);
            }

        } catch (Exception e) {
            log.error("Redis Message Subscriber Error: {}", e.getMessage());
        }
    }
}

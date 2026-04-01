package com.ozz.atlas.control.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ozz.atlas.control.config.RedisConstants;
import com.ozz.atlas.control.chat.service.RedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Control Service 전용 Redis 설정
 */
@Configuration
public class RedisConfig {

    /**
     * Redis 메시지 리스너 컨테이너
     * Control 서비스는 채팅방(chat:room:*)과 사용자 알림(notify:user:*) 패턴을 구독합니다.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 채팅방 동적 토픽 구독
        container.addMessageListener(listenerAdapter, new PatternTopic(RedisConstants.CHAT_ROOM_TOPIC_PREFIX + "*"));
        
        // 실시간 사용자 알림 구독
        container.addMessageListener(listenerAdapter, new PatternTopic(RedisConstants.NOTIFY_USER_TOPIC_PREFIX + "*"));
        
        return container;
    }

    /**
     * 비즈니스 로직을 처리하는 Subscriber 연결
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    /**
     * RedisTemplate 설정
     * Key: String, Value: JSON (Jackson2) 표준 사용
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        
        // Key는 문자열로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        
        // 💡 LocalDateTime 등 Java 8 날짜/시간 타입을 지원하도록 ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 날짜 모듈 등록
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 날짜를 문자열 포맷으로 저장
        
        // 직렬화 설정에 ObjectMapper 적용
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);
        
        return redisTemplate;
    }
}

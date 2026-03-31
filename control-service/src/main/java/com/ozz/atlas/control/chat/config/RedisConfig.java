package com.ozz.atlas.control.chat.config;

import com.ozz.atlas.common.redis.CommonRedisConfig;
import com.ozz.atlas.common.redis.RedisConstants;
import com.ozz.atlas.control.chat.service.RedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Control Service 전용 Redis 설정
 * CommonRedisConfig를 상속/임포트하여 공통 설정을 활용합니다.
 */
@Configuration
@Import(CommonRedisConfig.class) // 공통 설정을 확실히 가져오도록 명시 (컴포넌트 스캔 외부일 경우 대비)
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
}

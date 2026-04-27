package com.ozz.atlas.control.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor stompAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 구독 요청 prefix: /sub
        config.enableSimpleBroker("/sub");
        // 메시지 발행 요청 prefix: /pub
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /ws-control은 채팅/알림 공용 엔드포인트, /ws-chat은 기존 FE 호환용으로 유지한다.
        registry.addEndpoint("/ws-control", "/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인터셉터 등록
        registration.interceptors(stompAuthInterceptor);
    }
}

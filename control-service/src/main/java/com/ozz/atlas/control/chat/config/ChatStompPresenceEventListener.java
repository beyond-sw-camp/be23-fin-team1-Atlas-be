package com.ozz.atlas.control.chat.config;

import com.ozz.atlas.control.chat.service.ChatPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 웹소켓(STOMP) 연결 상태를 감지하여 유저의 채팅방 '읽음/접속' 상태를 갱신합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatStompPresenceEventListener {

    private final ChatPresenceService chatPresenceService;
    
    // SessionID를 키로 하여 [구독중인 방 아이디]와 [유저 아이디] 매핑을 임시 저장
    // Disconnect 발생 시 어떤 방에서 나가야 하는지 알기 위함
    private final Map<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    private record SessionInfo(String roomPublicId, String userPublicId) {}

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        
        // 인터셉터(StompAuthInterceptor)가 토큰 검증 후 넣어둔 유저 아이디를 세션에서 꺼냄
        String userPublicId = (String) headerAccessor.getSessionAttributes().get("userPublicId");

        // 대상이 특정 채팅방 구독(/sub/chat.room.{roomPublicId})인 경우
        if (destination != null && destination.startsWith("/sub/chat.room.") && userPublicId != null) {
            String roomPublicId = destination.replace("/sub/chat.room.", "");
            
            // Redis에 입장 기록
            chatPresenceService.addViewingUser(roomPublicId, userPublicId);
            
            // 세션 맵에 저장 (Disconnect 때 활용)
            sessionMap.put(sessionId, new SessionInfo(roomPublicId, userPublicId));
        }
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        removePresence(sessionId);
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        removePresence(sessionId);
    }

    private void removePresence(String sessionId) {
        SessionInfo sessionInfo = sessionMap.remove(sessionId);
        if (sessionInfo != null) {
            chatPresenceService.removeViewingUser(sessionInfo.roomPublicId(), sessionInfo.userPublicId());
        }
    }
}

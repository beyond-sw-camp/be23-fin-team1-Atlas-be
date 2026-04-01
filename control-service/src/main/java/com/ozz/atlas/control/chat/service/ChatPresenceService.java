package com.ozz.atlas.control.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 실시간 접속 상태(Presence)를 관리하는 서비스
 * Redis Set을 사용하여 특정 채팅방을 현재 보고 있는 유저 목록을 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRESENCE_KEY_PREFIX = "chat:presence:room:";

    /**
     * 유저가 채팅방에 입장(구독)했을 때 상태 기록
     */
    public void addViewingUser(String roomPublicId, String userPublicId) {
        String key = PRESENCE_KEY_PREFIX + roomPublicId;
        redisTemplate.opsForSet().add(key, userPublicId);
        log.debug("User {} entered room {}", userPublicId, roomPublicId);
    }

    /**
     * 유저가 채팅방을 나갔거나(구독 해제) 웹소켓이 끊겼을 때 상태 제거
     */
    public void removeViewingUser(String roomPublicId, String userPublicId) {
        String key = PRESENCE_KEY_PREFIX + roomPublicId;
        redisTemplate.opsForSet().remove(key, userPublicId);
        log.debug("User {} left room {}", userPublicId, roomPublicId);
    }

    /**
     * 현재 채팅방을 보고 있는 유저인지 확인
     */
    public boolean isViewing(String roomPublicId, String userPublicId) {
        String key = PRESENCE_KEY_PREFIX + roomPublicId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userPublicId);
        return isMember != null && isMember;
    }

    /**
     * 현재 채팅방을 보고 있는 모든 유저 목록 조회
     */
    public Set<String> getViewingUsers(String roomPublicId) {
        String key = PRESENCE_KEY_PREFIX + roomPublicId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) return Set.of();
        
        return members.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}

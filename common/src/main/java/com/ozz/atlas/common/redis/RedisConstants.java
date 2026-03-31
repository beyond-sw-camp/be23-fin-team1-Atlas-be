package com.ozz.atlas.common.redis;

/**
 * Redis 관련 상수 및 토픽 패턴 정의
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    /**
     * 채팅방별 메시지 발행/구독 토픽 패턴
     * 형식: chat:room:{roomPublicId}
     */
    public static final String CHAT_ROOM_TOPIC_PREFIX = "chat:room:";

    /**
     * 사용자별 실시간 알림 발행/구독 토픽 패턴
     * 형식: notify:user:{userPublicId}
     */
    public static final String NOTIFY_USER_TOPIC_PREFIX = "notify:user:";

    /**
     * 채팅방 토픽 생성 유틸리티
     */
    public static String getChatRoomTopic(String roomPublicId) {
        return CHAT_ROOM_TOPIC_PREFIX + roomPublicId;
    }

    /**
     * 사용자 알림 토픽 생성 유틸리티
     */
    public static String getNotifyUserTopic(String userPublicId) {
        return NOTIFY_USER_TOPIC_PREFIX + userPublicId;
    }
}

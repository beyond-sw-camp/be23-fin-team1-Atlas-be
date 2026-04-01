package com.ozz.atlas.control.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * STOMP 연결 시 JWT 토큰 서명을 검증하고 publicId를 추출하는 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    @Value("${jwt.access-token-secret}")
    private String secretKey;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("WebSocket CONNECT attempt with Authorization header");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("Extracted Token: {}...", token.substring(0, 10)); // 토큰 앞부분만 출력
                
                try {
                    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
                    log.info("Using Secret Key: {}...", secretKey.substring(0, 5));

                    // 2. JWT 서명 검증 및 파싱
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    // 3. Payload에서 'userPublicId' 추출 (auth-service 규격 준수)
                    String userPublicId = claims.get("userPublicId", String.class);
                    
                    if (userPublicId != null) {
                        log.info("WebSocket Authenticated. User Public ID: {}", userPublicId);
                        // 4. STOMP 세션 속성에 저장 (Subscribe/Disconnect 이벤트에서 사용)
                        accessor.getSessionAttributes().put("userPublicId", userPublicId);
                    } else {
                        log.warn("JWT Payload does not contain userPublicId");
                        throw new IllegalArgumentException("인증 정보에 유저 식별자가 없습니다.");
                    }

                } catch (JwtException e) {
                    log.error("WebSocket JWT Signature Verification Failed: {}", e.getMessage());
                    throw new IllegalArgumentException("유효하지 않은 인증 토큰입니다.");
                } catch (Exception e) {
                    log.error("WebSocket Auth Error", e);
                    throw new IllegalArgumentException("인증 처리 중 오류가 발생했습니다.");
                }
            } else {
                log.warn("WebSocket CONNECT denied: Missing Authorization header");
                // 실무 보안 정책에 따라 예외를 던져 연결을 거부할 수 있습니다.
                // throw new IllegalArgumentException("인증 정보가 필요합니다.");
            }
        }
        return message;
    }
}

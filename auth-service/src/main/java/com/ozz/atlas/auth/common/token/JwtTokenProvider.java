package com.ozz.atlas.auth.common.token;

import com.ozz.atlas.auth.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    @Value("${jwt.access-token-secret}")
    private String accessTokenSecret;

    @Value("${jwt.refresh-token-secret}")
    private String refreshTokenSecret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private final RedisTemplate<String, String> redisTemplate;

    private SecretKey accessKey;

    private SecretKey refreshKey;

    public JwtTokenProvider(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessTokenSecret));
        refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshTokenSecret));
    }

    // 로그인 성공 시 access token 생성
    public String createAccessToken(Long userId, String userPublicId, String organizationPublicId, String role) {
        Date now = new Date();

        return Jwts.builder()
                // subject에는 userId 저장
                .subject(String.valueOf(userId))
                // role,publicId claim으로 저장
                .claim("role", role)
                .claim("userPublicId", userPublicId)
                .claim("organizationPublicId", organizationPublicId)
                // 발급 시간
                .issuedAt(now)
                // 만료 시간
                .expiration(new Date(now.getTime() + accessTokenExpirationMs))
                // accessKey로 서명
                .signWith(accessKey)
                .compact();
    }

    // 로그인 성공 시 refresh token 생성
    public String createRefreshToken(Long userId) {
        Date now = new Date();

        String refreshToken = Jwts.builder()
                // refresh token도 subject에 userId 저장
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpirationMs))
                // refreshKey로 서명
                .signWith(refreshKey)
                .compact();

        // Redis에 userId를 key로 해서 refresh token 저장
        redisTemplate.opsForValue().set(
                String.valueOf(userId),
                refreshToken,
                refreshTokenExpirationMs,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    // refresh token이 유효한지 확인
    public boolean validateRefreshToken(String refreshToken) {
        try {
            // refresh token 파싱 및 서명 검증
            Claims claims = Jwts.parser()
                    .verifyWith(refreshKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String userId = claims.getSubject();

            // Redis에 저장된 refresh token과 비교
            String savedToken = redisTemplate.opsForValue().get(userId);

            return refreshToken.equals(savedToken);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // refresh token에서 userId 추출
    public Long getUserIdFromRefreshToken(String refreshToken) {
        Claims claims = Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
    private Claims getClaimsFromAccessToken(String accessToken) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }


}

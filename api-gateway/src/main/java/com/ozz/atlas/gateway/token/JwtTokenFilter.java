package com.ozz.atlas.gateway.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
public class JwtTokenFilter implements GlobalFilter {

    @Value("${jwt.access-token-secret}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String bearerToken = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        String urlPath = exchange.getRequest().getURI().getRawPath();

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())
                || "/api/auth/login".equals(urlPath)
                || "/api/auth/users".equals(urlPath)
                || urlPath.startsWith("/ws-chat")) {
            return chain.filter(exchange);
        }
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("token이 없거나, 형식이 잘못되었습니다.");
            }

            String token = bearerToken.substring(7);

            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String userPublicId = claims.get("userPublicId", String.class);
            String organizationPublicId = claims.get("organizationPublicId", String.class);
            String role = claims.get("role", String.class);

            ServerWebExchange serverWebExchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Public-Id");
                        headers.remove("X-Organization-Public-Id");
                        headers.remove("X-User-Role");
                        headers.set("X-User-Id", userId);
                        headers.set("X-User-Public-Id", userPublicId);
                        headers.set("X-Organization-Public-Id", organizationPublicId);
                        headers.set("X-User-Role", role);
                    }))
                    .build();

            return chain.filter(serverWebExchange);

        } catch (JwtException | IllegalArgumentException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}

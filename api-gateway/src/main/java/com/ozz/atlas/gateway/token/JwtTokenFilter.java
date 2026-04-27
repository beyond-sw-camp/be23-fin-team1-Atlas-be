package com.ozz.atlas.gateway.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtTokenFilter implements GlobalFilter {

    private final RoleRouteGuard roleRouteGuard;

    @Value("${jwt.access-token-secret}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String bearerToken = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        String urlPath = exchange.getRequest().getURI().getRawPath();

        HttpMethod method = exchange.getRequest().getMethod();

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())
                || (HttpMethod.POST.equals(method) && "/api/auth/login".equals(urlPath))
                || (HttpMethod.POST.equals(method) && "/api/auth/login/verify-ip".equals(urlPath))
                || (HttpMethod.POST.equals(method) && "/api/auth/users".equals(urlPath))
                || (HttpMethod.POST.equals(method) && "/api/auth/refresh".equals(urlPath))
                || urlPath.startsWith("/api/control/ws-control")
                || urlPath.startsWith("/api/control/ws-chat")
                || urlPath.startsWith("/ws-control")
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
            String organizationType = claims.get("organizationType", String.class);
            String role = claims.get("role", String.class);

            // 역할별로 접근이 제한된 서비스 경로는 gateway에서 먼저 차단한다.
            roleRouteGuard.validate(role, method, urlPath);

            ServerWebExchange serverWebExchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Public-Id");
                        headers.remove("X-Organization-Public-Id");
                        headers.remove("X-Organization-Type");
                        headers.remove("X-User-Role");
                        headers.set("X-User-Id", userId);
                        headers.set("X-User-Public-Id", userPublicId);
                        headers.set("X-Organization-Public-Id", organizationPublicId);
                        headers.set("X-User-Role", role);

                        if (organizationType != null) {
                            headers.set("X-Organization-Type", organizationType);
                        }
                    }))
                    .build();

            return chain.filter(serverWebExchange);

        } catch (org.springframework.web.server.ResponseStatusException e) {
            exchange.getResponse().setStatusCode(e.getStatusCode());
            return exchange.getResponse().setComplete();
        } catch (JwtException | IllegalArgumentException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}

package com.ozz.atlas.auth.common.config;

import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.common.jpa.Status;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.time.ZoneId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import java.util.Map;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String accessToken = authorization.substring(7);

            if (jwtTokenProvider.validateAccessToken(accessToken)) {
                Long userId = jwtTokenProvider.getUserIdFromAccessToken(accessToken);
                User user = userRepository.findWithOrganizationByUserId(userId).orElse(null);


                // 사용자 자체가 없거나,
                // 사용자 상태가 활성 상태가 아니거나,
                // 소속 조직 상태가 활성 상태가 아니면 인증을 막음
                if (user == null
                        || user.getStatus() != Status.ACTIVE
                        || user.getOrganization().getStatus() != Status.ACTIVE) {

                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                Date issuedAt = jwtTokenProvider.getIssuedAtFromAccessToken(accessToken);

                Instant tokenIssuedAt = issuedAt.toInstant()
                        .truncatedTo(ChronoUnit.SECONDS);
// 다른 곳에서 더 나중에 로그인했으면 기존 토큰 차단
                if (user.getLastLoginAt() != null) {
                    Instant lastLoginAt = user.getLastLoginAt()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .truncatedTo(ChronoUnit.SECONDS);

                    if (tokenIssuedAt.isBefore(lastLoginAt)) {
                        SecurityContextHolder.clearContext();

                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");

                        objectMapper.writeValue(response.getWriter(), Map.of(
                                "code", "DUPLICATE_LOGIN_SESSION_EXPIRED",
                                "message", "다른 기기에서 로그인되어 로그아웃되었습니다. 다시 로그인해 주세요."
                        ));

                        return;
                    }
                }

// 다른 곳에서 더 나중에 로그인했으면 기존 토큰 차단
                if (user.getLastLoginAt() != null) {
                    Instant lastLoginAt = user.getLastLoginAt()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .truncatedTo(ChronoUnit.SECONDS);

                    if (tokenIssuedAt.isBefore(lastLoginAt)) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }

                String userPublicId = user.getPublicId();
                String organizationPublicId = user.getOrganization().getPublicId();
                UserRole role = user.getUserRole();

                AuthPrincipal principal = new AuthPrincipal(
                        user.getUserId(),
                        userPublicId,
                        organizationPublicId,
                        role
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
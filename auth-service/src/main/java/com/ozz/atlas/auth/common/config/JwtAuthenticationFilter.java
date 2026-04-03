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
import java.util.List;
import java.time.ZoneId;
import java.util.Date;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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


                if (user == null || user.getStatus() != Status.ACTIVE) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                Date issuedAt = jwtTokenProvider.getIssuedAtFromAccessToken(accessToken);

                if (user.getPasswordChangedAt() != null &&
                        issuedAt.toInstant().isBefore(
                                user.getPasswordChangedAt().atZone(ZoneId.systemDefault()).toInstant()
                        )) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
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
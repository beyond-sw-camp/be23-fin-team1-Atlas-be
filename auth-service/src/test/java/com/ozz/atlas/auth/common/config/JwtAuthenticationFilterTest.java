package com.ozz.atlas.auth.common.config;

import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);

    @Test
    void refreshRequestSkipsAccessTokenValidation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/refresh");
        request.addHeader("Authorization", "Bearer expired-or-older-access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(filterChain.getRequest()).isSameAs(request);
        verify(jwtTokenProvider, never()).validateAccessToken("expired-or-older-access-token");
    }

    @Test
    void protectedRequestWithOlderAccessTokenReturnsDuplicateLoginUnauthorized() throws Exception {
        String accessToken = "older-access-token";
        Long userId = 1L;
        Instant tokenIssuedAt = Instant.parse("2026-05-06T00:00:00Z");
        LocalDateTime lastLoginAt = LocalDateTime.ofInstant(
                Instant.parse("2026-05-06T00:00:10Z"),
                ZoneId.systemDefault()
        );
        User user = activeUser(userId, lastLoginAt);

        when(jwtTokenProvider.validateAccessToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromAccessToken(accessToken)).thenReturn(userId);
        when(jwtTokenProvider.getIssuedAtFromAccessToken(accessToken)).thenReturn(Date.from(tokenIssuedAt));
        when(userRepository.findWithOrganizationByUserId(userId)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("DUPLICATE_LOGIN_SESSION_EXPIRED");
        assertThat(filterChain.getRequest()).isNull();
    }

    private User activeUser(Long userId, LocalDateTime lastLoginAt) {
        Organization organization = Organization.builder()
                .organizationType(OrganizationType.BUYER)
                .organizationName("Atlas Buyer")
                .organizationEnglishName("Atlas Buyer")
                .organizationAlias("BUYER")
                .contactFirstName("Atlas")
                .contactLastName("Owner")
                .contactPhone("010-0000-0000")
                .build();

        User user = User.builder()
                .userId(userId)
                .organization(organization)
                .loginId("atlas_user")
                .password("password")
                .firstName("Atlas")
                .lastName("User")
                .email("atlas@example.com")
                .phone("010-1111-1111")
                .userRole(UserRole.USER)
                .build();
        user.markLoggedInAt(lastLoginAt);

        return user;
    }
}

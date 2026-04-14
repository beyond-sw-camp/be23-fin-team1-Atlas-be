package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.service.AuthService;
import com.ozz.atlas.auth.service.LoginHistoryService;
import com.ozz.atlas.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginHistoryService loginHistoryService;

    @Autowired
    public AuthController(AuthService authService, UserService userService, JwtTokenProvider jwtTokenProvider, LoginHistoryService loginHistoryService) {
        this.authService = authService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginHistoryService = loginHistoryService;
    }

    //    로그인
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginDto dto,
                                          HttpServletRequest request) {
        User user = authService.login(dto.getLoginId(), dto.getPassword());

        loginHistoryService.saveSuccess(
                user,
                extractClientIp(request),
                request.getHeader("User-Agent")
        );

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getPublicId(),
                user.getOrganization().getPublicId(),
                user.getOrganization().getOrganizationType().name(),
                user.getUserRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        TokenDto tokenDto = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(tokenDto);
    }


    //    사용자 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthPrincipal principal) {
        authService.logout(principal);
        return ResponseEntity.noContent().build();
    }

    //    토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDto> refresh(@RequestBody @Valid RefreshTokenRequestDto dto) {
        AccessTokenResponseDto response = authService.refresh(dto.getRefreshToken());
        return ResponseEntity.ok(response);
    }


    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


    //    사용자 로그인 이력 조회
    @GetMapping("/users/{userId}/login-histories")
    public ResponseEntity<Page<LoginHistoryListDto>> userLoginHistories(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthPrincipal principal,
            @PageableDefault(size = 10, sort = "loginHistoryId", direction = Sort.Direction.DESC) Pageable pageable) {

        if (principal.role() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<LoginHistoryListDto> response = loginHistoryService.userLoginHistories(userId, pageable);
        return ResponseEntity.ok(response);
    }




}

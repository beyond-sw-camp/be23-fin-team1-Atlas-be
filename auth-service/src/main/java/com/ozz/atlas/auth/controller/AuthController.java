package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.auth.*;
import com.ozz.atlas.auth.dtos.history.LoginHistoryListDto;
import com.ozz.atlas.auth.service.AuthService;
import com.ozz.atlas.auth.service.LoginHistoryService;
import com.ozz.atlas.auth.common.exception.LoginFailedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import com.ozz.atlas.auth.domain.LoginVerification;
import com.ozz.atlas.auth.service.LoginVerificationService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginHistoryService loginHistoryService;
    private final LoginVerificationService loginVerificationService;

    @Autowired
    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider, LoginHistoryService loginHistoryService, LoginVerificationService loginVerificationService) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginHistoryService = loginHistoryService;
        this.loginVerificationService = loginVerificationService;
    }

    //    로그인
    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "로그인",
            description = "로그인 ID와 비밀번호로 로그인합니다. 새 IP 이면 이메일 인증 단계로 전환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "loginId": "atlas_admin",
                                              "password": "Atlas!234"
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 또는 새 IP 인증 필요",
                    content = @Content(schema = @Schema(implementation = TokenDto.class))
            )
    )
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginDto dto, HttpServletRequest request) {
        // 로그인 성공/실패에서 같이 쓸 요청 정보
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            // 아이디/비밀번호 검증을 먼저 수행
            User user = authService.login(dto.getLoginId(), dto.getPassword());

            // 최근 성공 로그인 IP 와 현재 IP 가 다르면 이메일 인증 단계로 넘김
            if (loginHistoryService.requiresIpVerification(user, clientIp)) {
                LoginVerification verification =
                        loginVerificationService.createVerification(user, clientIp, userAgent);

                return ResponseEntity.ok(
                        TokenDto.builder()
                                .passwordChangeRequired(false)
                                .ipVerificationRequired(true)
                                .verificationRequestId(verification.getVerificationRequestId())
                                .verificationExpiresAt(verification.getExpiresAt())
                                .build()
                );
            }

            // 같은 IP 이거나 첫 로그인 성공이면 바로 로그인 성공 처리
            loginHistoryService.saveSuccess(user, clientIp, userAgent);

            return ResponseEntity.ok(buildTokenResponse(user));
        } catch (LoginFailedException e) {
            // 사용자 식별이 되는 실패만 로그인 실패 이력에 남김
            if (e.getUser() != null) {
                loginHistoryService.saveFailure(e.getUser(), clientIp, userAgent, e.getFailureReason());
            }

            throw e;
        }
    }

    //    사용자 로그아웃
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인한 사용자의 Refresh Token을 폐기한다."
    )
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthPrincipal principal) {
        authService.logout(principal);
        return ResponseEntity.noContent().build();
    }

    //    토큰 재발급
    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token으로 새 Access Token을 발급한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RefreshTokenRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token"
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = AccessTokenResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.new-access-token"
                                            }
                                            """
                            )
                    )
            )
    )
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
    @Operation(
            summary = "사용자 로그인 이력 조회",
            description = "플랫폼 관리자가 특정 사용자의 로그인 이력을 조회한다."
    )
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

    @PostMapping("/login/verify-ip")
    @SecurityRequirements
    @Operation(
            summary = "새 IP 로그인 이메일 인증",
            description = "새 IP 로그인 시 이메일로 받은 인증 코드를 검증하고 토큰을 발급합니다."
    )
    public ResponseEntity<TokenDto> verifyLoginIp(
            @RequestBody @Valid LoginIpVerifyDto dto,
            HttpServletRequest request
    ) {
        // 인증 실패 이력에도 현재 요청의 IP/브라우저를 남김
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            // 인증 요청과 인증 코드를 검증
            LoginVerification verification = loginVerificationService.verify(
                    dto.getVerificationRequestId(),
                    dto.getVerificationCode()
            );

            User user = verification.getUser();

// 토큰을 먼저 만듬
// 여기서 예외가 나면 인증 요청을 아직 지우지 않아서 원인 파악이 쉬워짐
            TokenDto tokenResponse = buildTokenResponse(user);

// 인증이 성공했으면 로그인 성공 이력을 남김
            loginHistoryService.saveSuccess(user, verification.getIpAddress(), verification.getUserAgent());

// 사용한 인증 요청은 마지막에 삭제
            loginVerificationService.consume(verification);

// 토큰을 반환
            return ResponseEntity.ok(tokenResponse);

        } catch (LoginFailedException e) {
            // 인증 실패도 로그인 실패 이력에 남김
            if (e.getUser() != null) {
                loginHistoryService.saveFailure(e.getUser(), clientIp, userAgent, e.getFailureReason());
            }

            throw e;
        }

    }


    // 로그인 성공 시 공통 토큰 응답을 만듭니다.
    private TokenDto buildTokenResponse(User user) {
        // access token 에 넣을 발급 시각을 딱 한 번만 만듭니다.
        // 이 시각을 DB 저장과 JWT 발급에 같이 씁니다.
        Date issuedAt = new Date();

        // JWT 발급 시각과 완전히 같은 순간을 DB 에도 저장하기 위해
        // Date 를 LocalDateTime 으로 바꿉니다.
        LocalDateTime loginAt = LocalDateTime.ofInstant(
                issuedAt.toInstant(),
                ZoneId.systemDefault()
        );

        // 마지막 로그인 성공 시각을 저장합니다.
        authService.markLoginSuccess(user.getUserId(), loginAt);

        // 방금 저장한 시각과 동일한 issuedAt 으로 access token 을 만듭니다.
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getPublicId(),
                user.getOrganization().getPublicId(),
                user.getOrganization().getOrganizationType().name(),
                user.getUserRole().name(),
                issuedAt
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .passwordChangeRequired(user.isPasswordChangeRequired())
                .ipVerificationRequired(false)
                .build();
    }



}

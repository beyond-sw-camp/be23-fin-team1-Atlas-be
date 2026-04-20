package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.service.AuthService;
import com.ozz.atlas.auth.service.LoginHistoryService;
import com.ozz.atlas.auth.service.UserService;
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


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginHistoryService loginHistoryService;

    @Autowired
    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider, LoginHistoryService loginHistoryService) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginHistoryService = loginHistoryService;
    }

    //    로그인
    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "로그인",
            description = "로그인 ID와 비밀번호로 Access Token과 Refresh Token을 발급한다.",
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
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = TokenDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.access-token",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token"
                                            }
                                            """
                            )
                    )
            )
    )
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
                .passwordChangeRequired(user.isPasswordChangeRequired())
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

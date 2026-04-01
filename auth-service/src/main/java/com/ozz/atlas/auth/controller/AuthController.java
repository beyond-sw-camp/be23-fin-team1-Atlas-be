package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.service.AuthService;
import com.ozz.atlas.auth.service.OrganizationService;
import com.ozz.atlas.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final OrganizationService organizationService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public AuthController(AuthService authService, UserService userService, OrganizationService organizationService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userService = userService;
        this.organizationService = organizationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginDto dto) {
        User user = authService.login(dto.getLoginId(), dto.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUserRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        TokenDto tokenDto = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(tokenDto);
    }

    @PostMapping("/organizations")
    public ResponseEntity<OrganizationCreateResponseDto> createOrganization(
            @RequestHeader("X-User-Role") String role,
            @RequestBody @Valid OrganizationCreateDto dto) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String organizationPublicId = organizationService.createOrganization(dto);
        OrganizationCreateResponseDto response = OrganizationCreateResponseDto.builder()
                .organizationPublicId(organizationPublicId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/users")
    public ResponseEntity<UserCreateResponseDto> signup(@RequestBody @Valid UserSignUpDto dto) {
        String userPublicId = userService.signup(dto);

        UserCreateResponseDto response = UserCreateResponseDto.builder()
                .userPublicId(userPublicId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

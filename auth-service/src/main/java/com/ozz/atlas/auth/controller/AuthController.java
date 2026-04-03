package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.service.AuthService;
import com.ozz.atlas.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(AuthService authService, UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //    로그인
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginDto dto) {
        User user = authService.login(dto.getLoginId(), dto.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getPublicId(),
                user.getOrganization().getPublicId(),
                user.getUserRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        TokenDto tokenDto = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(tokenDto);
    }

}

package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.auth.AccessTokenResponseDto;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ozz.atlas.auth.common.exception.LoginFailedException;



@Service
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;

    }

    //    사용자 로그인
    public User login(String loginId, String password) {
        User user = userRepository.findByLoginId(loginId)
                // 없는 아이디는 사용자 식별이 안 되므로 이력 저장 대상에서 제외
                .orElseThrow(() -> new LoginFailedException(
                        "아이디 또는 비밀번호가 올바르지 않습니다.",
                        "INVALID_LOGIN_ID",
                        null
                ));

        // 비밀번호가 틀리면 사용자 정보가 있으므로 실패 이력을 남김
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LoginFailedException(
                    "아이디 또는 비밀번호가 올바르지 않습니다.",
                    "INVALID_PASSWORD",
                    user
            );
        }

        // 비활성화 계정도 사용자 정보가 있으므로 실패 이력을 남김
        if (user.getStatus() != Status.ACTIVE) {
            throw new LoginFailedException(
                    "비활성화 또는 삭제된 사용자입니다.",
                    "INACTIVE_USER",
                    user
            );
        }
        // 사용자 계정이 활성이어도 소속 조직이 비활성이면 로그인할 수 없음
        if (user.getOrganization().getStatus() != Status.ACTIVE) {
            throw new LoginFailedException(
                    "비활성화 또는 삭제된 조직입니다.",
                    "INACTIVE_ORGANIZATION",
                    user
            );
        }


        user.getOrganization().getPublicId();

        return user;
    }


    //    사용자 로그아웃
    public void logout(AuthPrincipal principal) {
        jwtTokenProvider.revokeRefreshToken(principal.userId());
    }

    //    토큰 재발급
    public AccessTokenResponseDto refresh(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 refresh token입니다.");
        }

        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        User user = userRepository.findWithOrganizationByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("비활성화 또는 삭제된 사용자입니다.");
        }
        // 사용자가 살아 있어도 소속 조직이 비활성이면 토큰 재발급을 막음
        if (user.getOrganization().getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("비활성화 또는 삭제된 조직입니다.");
        }


        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getPublicId(),
                user.getOrganization().getPublicId(),
                user.getOrganization().getOrganizationType().name(),
                user.getUserRole().name()
        );

        return AccessTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .build();
    }


}

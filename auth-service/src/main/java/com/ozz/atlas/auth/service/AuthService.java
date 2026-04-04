package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userRepository.findByLoginId(loginId).
                orElseThrow(() -> new IllegalArgumentException("아이디 틀림"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 틀림");
        }
        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("비활성화 또는 삭제된 사용자입니다.");
        }

        user.getOrganization().getPublicId();
        return user;
    }

    //    사용자 로그아웃
    public void logout(AuthPrincipal principal) {
        jwtTokenProvider.revokeRefreshToken(principal.userId());
    }

}

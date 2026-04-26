package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.PasswordChangeVerification;
import com.ozz.atlas.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordChangeVerificationRepository
        extends JpaRepository<PasswordChangeVerification, String> {

    // 같은 사용자의 이전 비밀번호 변경 인증 요청이 남아 있으면 먼저 지움
    void deleteByUser(User user);
}

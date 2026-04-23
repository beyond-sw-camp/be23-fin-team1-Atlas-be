package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.LoginVerification;
import com.ozz.atlas.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginVerificationRepository extends JpaRepository<LoginVerification, String> {

    // 같은 사용자에게 남아 있는 이전 인증 요청을 지움
    void deleteByUser(User user);
}

package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.exception.LoginFailedException;
import com.ozz.atlas.auth.domain.LoginVerification;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.repository.LoginVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class LoginVerificationService {

    // 인증 코드는 3분 안에 입력
    private static final int EXPIRE_MINUTES = 3;

    private final LoginVerificationRepository loginVerificationRepository;
    private final CredentialMailService credentialMailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public LoginVerificationService(
            LoginVerificationRepository loginVerificationRepository,
            CredentialMailService credentialMailService
    ) {
        this.loginVerificationRepository = loginVerificationRepository;
        this.credentialMailService = credentialMailService;
    }

    // 새 IP 로그인용 이메일 인증 요청을 생성
    public LoginVerification createVerification(User user, String ipAddress, String userAgent) {
        // 이전에 남아 있던 같은 사용자 인증 요청은 지움
        loginVerificationRepository.deleteByUser(user);

        // 6자리 숫자 코드를 만듬
        String verificationCode = generateVerificationCode();

        // 인증 요청 ID 를 새로 만듬
        String verificationRequestId = UUID.randomUUID().toString();

        // 3분 뒤 만료되도록 만듬
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);

        // 인증 요청을 저장
        LoginVerification verification = LoginVerification.builder()
                .verificationRequestId(verificationRequestId)
                .user(user)
                .verificationCode(verificationCode)
                .ipAddress(ipAddress)
                .userAgent(userAgent != null ? userAgent : "UNKNOWN")
                .expiresAt(expiresAt)
                .build();

        loginVerificationRepository.save(verification);

        // 사용자 이메일로 인증 코드를 보냄
        credentialMailService.sendLoginVerificationMail(
                user.getEmail(),
                user.getLoginId(),
                verificationCode,
                EXPIRE_MINUTES
        );

        return verification;
    }

    // 인증 요청을 검증
    public LoginVerification verify(String verificationRequestId, String verificationCode) {
        // 요청 ID 로 인증 요청을 찾음
        LoginVerification verification = loginVerificationRepository.findById(verificationRequestId)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청을 찾을 수 없습니다. 다시 로그인해 주세요."));

        // 만료되었으면 실패 처리하고 요청을 지움
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            loginVerificationRepository.delete(verification);

            throw new LoginFailedException(
                    "이메일 인증 시간이 만료되었습니다. 다시 로그인해 주세요.",
                    "IP_VERIFICATION_EXPIRED",
                    verification.getUser()
            );
        }

        // 코드가 다르면 실패 처리하고 요청을 지움
        if (!verification.getVerificationCode().equals(verificationCode)) {
            loginVerificationRepository.delete(verification);

            throw new LoginFailedException(
                    "이메일 인증 코드가 올바르지 않습니다. 다시 로그인해 주세요.",
                    "IP_VERIFICATION_CODE_MISMATCH",
                    verification.getUser()
            );
        }

        return verification;
    }

    // 인증이 끝난 요청은 삭제
    public void consume(LoginVerification verification) {
        loginVerificationRepository.delete(verification);
    }

    // 6자리 숫자 인증 코드를 생성
    private String generateVerificationCode() {
        int number = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(number);
    }
}

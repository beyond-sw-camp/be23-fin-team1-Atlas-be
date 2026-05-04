package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.exception.LoginFailedException;
import com.ozz.atlas.auth.domain.LoginVerification;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginVerificationService {

    // 인증 코드는 3분 안에 입력
    private static final int EXPIRE_MINUTES = 3;
    private static final String VERIFICATION_KEY_PREFIX = "auth:login-verification:";
    private static final String USER_INDEX_KEY_PREFIX = "auth:login-verification:user:";

    private final StringRedisTemplate verificationRedisTemplate;
    private final UserRepository userRepository;
    private final CredentialMailService credentialMailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public LoginVerificationService(
            @Qualifier("authVerificationStringRedisTemplate") StringRedisTemplate verificationRedisTemplate,
            UserRepository userRepository,
            CredentialMailService credentialMailService
    ) {
        this.verificationRedisTemplate = verificationRedisTemplate;
        this.userRepository = userRepository;
        this.credentialMailService = credentialMailService;
    }

    // 새 IP 로그인용 이메일 인증 요청을 생성
    public LoginVerification createVerification(User user, String ipAddress, String userAgent) {
        deletePreviousVerification(user.getUserId());

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

        String key = verificationKey(verificationRequestId);
        Map<String, String> verificationPayload = new HashMap<>();
        verificationPayload.put("userId", String.valueOf(user.getUserId()));
        verificationPayload.put("verificationCode", verificationCode);
        verificationPayload.put("ipAddress", ipAddress != null ? ipAddress : "UNKNOWN");
        verificationPayload.put("userAgent", userAgent != null ? userAgent : "UNKNOWN");
        verificationPayload.put("expiresAt", expiresAt.toString());
        verificationRedisTemplate.opsForHash().putAll(key, verificationPayload);
        verificationRedisTemplate.expire(key, EXPIRE_MINUTES, TimeUnit.MINUTES);
        verificationRedisTemplate.opsForValue().set(
                userIndexKey(user.getUserId()),
                verificationRequestId,
                EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

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
        String key = verificationKey(verificationRequestId);
        Map<Object, Object> storedVerification = verificationRedisTemplate.opsForHash().entries(key);
        if (storedVerification.isEmpty()) {
            throw new IllegalArgumentException("인증 요청을 찾을 수 없습니다. 다시 로그인해 주세요.");
        }

        Long userId = Long.valueOf((String) storedVerification.get("userId"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청을 찾을 수 없습니다. 다시 로그인해 주세요."));

        LocalDateTime expiresAt = LocalDateTime.parse((String) storedVerification.get("expiresAt"));

        // 만료됐으면 실패 처리하고 요청을 지웁니다.
        if (expiresAt.isBefore(LocalDateTime.now())) {
            deleteVerification(verificationRequestId, userId);

            throw new LoginFailedException(
                    "이메일 인증 시간이 만료되었습니다. 다시 로그인해 주세요.",
                    "IP_VERIFICATION_EXPIRED",
                    user
            );
        }

        // 코드가 다르면 실패 처리하고 요청을 지웁니다.
        if (!storedVerification.get("verificationCode").equals(verificationCode)) {
            deleteVerification(verificationRequestId, userId);

            throw new LoginFailedException(
                    "이메일 인증 코드가 올바르지 않습니다. 다시 로그인해 주세요.",
                    "IP_VERIFICATION_CODE_MISMATCH",
                    user
            );
        }

        // verify-ip 응답에서 토큰을 만들 때 organization 정보가 필요합니다.
        // open-in-view=false 라서 여기서 미리 organization 을 초기화해 둡니다.
        user.getOrganization().getPublicId();

        return LoginVerification.builder()
                .verificationRequestId(verificationRequestId)
                .user(user)
                .verificationCode((String) storedVerification.get("verificationCode"))
                .ipAddress((String) storedVerification.get("ipAddress"))
                .userAgent((String) storedVerification.get("userAgent"))
                .expiresAt(expiresAt)
                .build();
    }



    // 인증이 끝난 요청은 삭제
    public void consume(LoginVerification verification) {
        deleteVerification(verification.getVerificationRequestId(), verification.getUser().getUserId());
    }

    // 6자리 숫자 인증 코드를 생성
    private String generateVerificationCode() {
        int number = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private void deletePreviousVerification(Long userId) {
        String previousRequestId = verificationRedisTemplate.opsForValue().get(userIndexKey(userId));
        if (previousRequestId != null) {
            verificationRedisTemplate.delete(verificationKey(previousRequestId));
        }
        verificationRedisTemplate.delete(userIndexKey(userId));
    }

    private void deleteVerification(String verificationRequestId, Long userId) {
        verificationRedisTemplate.delete(verificationKey(verificationRequestId));
        verificationRedisTemplate.delete(userIndexKey(userId));
    }

    private String verificationKey(String verificationRequestId) {
        return VERIFICATION_KEY_PREFIX + verificationRequestId;
    }

    private String userIndexKey(Long userId) {
        return USER_INDEX_KEY_PREFIX + userId;
    }
}

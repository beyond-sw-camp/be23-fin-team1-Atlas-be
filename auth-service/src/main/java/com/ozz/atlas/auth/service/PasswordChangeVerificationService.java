package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.user.PasswordVerificationRequestResponseDto;
import com.ozz.atlas.auth.dtos.user.UserPasswordUpdateDto;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.dtos.user.PasswordVerificationConfirmDto;
import com.ozz.atlas.common.jpa.Status;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PasswordChangeVerificationService {

    // 인증코드는 3분 동안만 유효하게
    private static final int EXPIRE_MINUTES = 3;
    private static final String VERIFICATION_KEY_PREFIX = "auth:password-change-verification:";
    private static final String USER_INDEX_KEY_PREFIX = "auth:password-change-verification:user:";

    private final StringRedisTemplate verificationRedisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialMailService credentialMailService;
    private final JwtTokenProvider jwtTokenProvider;


    // 6자리 숫자 코드를 만들 때 사용할 보안 난수 객체
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordChangeVerificationService(
            @Qualifier("authVerificationStringRedisTemplate") StringRedisTemplate verificationRedisTemplate,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CredentialMailService credentialMailService, JwtTokenProvider jwtTokenProvider
    ) {
        this.verificationRedisTemplate = verificationRedisTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.credentialMailService = credentialMailService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 비밀번호 변경 인증 요청을 생성하고 인증코드를 이메일로 보냄
    public PasswordVerificationRequestResponseDto createVerificationRequest(
            Long userId,
            UserPasswordUpdateDto dto,
            AuthPrincipal principal,
            String ipAddress,
            String userAgent
    ) {
        // 요청한 사용자가 실제 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 본인만 자기 비밀번호 변경 인증을 요청할 수 있게 막음
        if (!principal.userId().equals(userId)) {
            throw new IllegalArgumentException("비밀번호 변경 권한이 없습니다.");
        }

        // 비활성화 또는 삭제된 사용자는 비밀번호 변경을 진행할 수 없게 막음
        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        // 강제 비밀번호 변경 상태가 아닐 때만 현재 비밀번호를 검사
        // 기존 userPasswordUpdate 로직과 같은 기준을 유지하는 부분
        if (!user.isPasswordChangeRequired()) {
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
        }

        // 새 비밀번호와 새 비밀번호 확인 값이 같은지 검사
        if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 같으면 바꿀 의미가 없으니 막음
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호와 다른 비밀번호를 입력해 주세요.");
        }

        deletePreviousVerification(user.getUserId());

        // 프론트가 나중에 인증 확인할 때 사용할 요청 ID
        String verificationRequestId = UUID.randomUUID().toString();

        // 이메일로 보낼 6자리 숫자 인증코드를 만듬
        String verificationCode = generateVerificationCode();

        // 새 비밀번호는 평문 저장을 하면 안 되므로 지금 바로 암호화
        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());

        // 인증코드 만료 시각을 3분
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);

        String key = verificationKey(verificationRequestId);
        Map<String, String> verificationPayload = new HashMap<>();
        verificationPayload.put("userId", String.valueOf(user.getUserId()));
        verificationPayload.put("verificationCode", verificationCode);
        verificationPayload.put("encodedNewPassword", encodedNewPassword);
        verificationPayload.put("expiresAt", expiresAt.toString());
        verificationPayload.put("ipAddress", ipAddress != null ? ipAddress : "UNKNOWN");
        verificationPayload.put("userAgent", userAgent != null ? userAgent : "UNKNOWN");
        verificationRedisTemplate.opsForHash().putAll(key, verificationPayload);
        verificationRedisTemplate.expire(key, EXPIRE_MINUTES, TimeUnit.MINUTES);
        verificationRedisTemplate.opsForValue().set(
                userIndexKey(user.getUserId()),
                verificationRequestId,
                EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        // 사용자 이메일로 인증코드를 발송
        credentialMailService.sendPasswordChangeVerificationMail(
                user.getEmail(),
                user.getLoginId(),
                verificationCode,
                EXPIRE_MINUTES
        );

        // 프론트가 다음 단계에서 쓸 요청 ID 와 만료 시각을 돌려줌
        return PasswordVerificationRequestResponseDto.builder()
                .verificationRequestId(verificationRequestId)
                .expiresAt(expiresAt)
                .build();
    }

    // 6자리 숫자 인증코드를 생성
    private String generateVerificationCode() {
        int number = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    // 이메일 인증코드를 확인하고 실제 비밀번호를 변경
    public void confirmVerification(
            Long userId,
            PasswordVerificationConfirmDto dto,
            AuthPrincipal principal
    ) {
        // 본인만 자기 비밀번호 변경 인증을 완료할 수 있게 막음
        if (!principal.userId().equals(userId)) {
            throw new IllegalArgumentException("비밀번호 변경 권한이 없습니다.");
        }

        String verificationRequestId = dto.getVerificationRequestId();
        Map<Object, Object> storedVerification = verificationRedisTemplate.opsForHash().entries(verificationKey(verificationRequestId));
        if (storedVerification.isEmpty()) {
            throw new IllegalArgumentException("비밀번호 변경 인증 요청을 찾을 수 없습니다.");
        }

        User user = userRepository.findById(Long.valueOf((String) storedVerification.get("userId")))
                .orElseThrow(() -> new IllegalArgumentException("비밀번호 변경 인증 요청을 찾을 수 없습니다."));

        // 요청한 사용자와 인증 요청의 사용자가 다르면 막음
        if (!user.getUserId().equals(userId)) {
            throw new IllegalArgumentException("잘못된 비밀번호 변경 인증 요청입니다.");
        }

        // 만료된 인증 요청이면 삭제하고 실패 처리
        LocalDateTime expiresAt = LocalDateTime.parse((String) storedVerification.get("expiresAt"));
        if (expiresAt.isBefore(LocalDateTime.now())) {
            deleteVerification(verificationRequestId, user.getUserId());
            throw new IllegalArgumentException("인증 시간이 만료되었습니다. 다시 요청해 주세요.");
        }

        // 인증코드가 다르면 실패 처리
        if (!storedVerification.get("verificationCode").equals(dto.getVerificationCode())) {
            throw new IllegalArgumentException("인증코드가 올바르지 않습니다.");
        }

        // 인증이 성공했으므로 저장해둔 새 비밀번호를 실제 비밀번호로 반영
        user.updatePassword((String) storedVerification.get("encodedNewPassword"));

        // 비밀번호 변경이 끝났으므로 강제 변경 상태가 있으면 해제
        user.clearPasswordChangeRequired();

        // 기존 refresh token 은 더 이상 쓰지 못하게 제거
        jwtTokenProvider.revokeRefreshToken(user.getUserId());

        // 사용한 인증 요청은 다시 못 쓰게 삭제
        deleteVerification(verificationRequestId, user.getUserId());
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

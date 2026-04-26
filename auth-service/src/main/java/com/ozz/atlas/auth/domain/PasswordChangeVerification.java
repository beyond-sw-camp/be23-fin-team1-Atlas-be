package com.ozz.atlas.auth.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeVerification extends BaseTimeEntity {

    @Id
    @Column(nullable = false, length = 36, updatable = false)
    private String verificationRequestId;

    // 어떤 사용자의 비밀번호 변경 요청인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 이메일로 보낼 6자리 인증코드
    @Column(nullable = false, length = 10)
    private String verificationCode;

    // 새 비밀번호는 평문으로 저장하면 안 되므로
    // 미리 암호화한 값만 저장
    @Column(nullable = false, length = 255)
    private String encodedNewPassword;

    // 인증코드가 언제 만료되는지 저장
    // 요구사항 기준으로 생성 시점 + 3분
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 어떤 IP 에서 요청했는지 저장
    @Column(nullable = false, length = 45)
    private String ipAddress;

    // 어떤 브라우저나 앱에서 요청했는지 저장
    @Column(nullable = false, length = 500)
    private String userAgent;
}

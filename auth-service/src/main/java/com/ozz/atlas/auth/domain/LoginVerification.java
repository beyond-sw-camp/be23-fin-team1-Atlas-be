package com.ozz.atlas.auth.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginVerification extends BaseTimeEntity {

    // 인증 요청 ID
    // 프론트가 이 값을 들고 있다가 인증 API 에 다시 보냄
    @Id
    @Column(nullable = false, length = 36, updatable = false)
    private String verificationRequestId;

    // 어떤 사용자의 새 IP 로그인인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 이메일로 보낸 인증 코드
    @Column(nullable = false, length = 10)
    private String verificationCode;

    // 로그인 시도 IP 를 저장
    @Column(nullable = false, length = 45)
    private String ipAddress;

    // 로그인 시도 브라우저를 저장
    @Column(nullable = false, length = 500)
    private String userAgent;

    // 인증 만료 시각
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}

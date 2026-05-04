package com.ozz.atlas.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginVerification {

    // 인증 요청 ID
    // 프론트가 이 값을 들고 있다가 인증 API 에 다시 보냄
    private String verificationRequestId;

    // 어떤 사용자의 새 IP 로그인인지 연결
    private User user;

    // 이메일로 보낸 인증 코드
    private String verificationCode;

    // 로그인 시도 IP 를 저장
    private String ipAddress;

    // 로그인 시도 브라우저를 저장
    private String userAgent;

    // 인증 만료 시각
    private LocalDateTime expiresAt;
}

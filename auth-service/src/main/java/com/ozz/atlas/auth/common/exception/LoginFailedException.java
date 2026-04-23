package com.ozz.atlas.auth.common.exception;

import com.ozz.atlas.auth.domain.User;
import lombok.Getter;

@Getter
public class LoginFailedException extends RuntimeException {

    // 화면과 로그에서 구분할 실패 코드
    private final String failureReason;

    // 실패 이력을 저장할 수 있는 사용자
    private final User user;

    public LoginFailedException(String message, String failureReason, User user) {
        super(message);
        this.failureReason = failureReason;
        this.user = user;
    }
}

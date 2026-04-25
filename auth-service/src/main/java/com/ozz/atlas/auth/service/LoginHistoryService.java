package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.LoginHistory;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.history.LoginHistoryListDto;
import com.ozz.atlas.auth.repository.LoginHistoryRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository, UserRepository userRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.userRepository = userRepository;
    }

    public void saveSuccess(User user, String ipAddress, String userAgent) {
        LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .failureReason(null)
                .ipAddress(ipAddress)
                .userAgent(userAgent != null ? userAgent : "UNKNOWN")
                .build();

        loginHistoryRepository.save(loginHistory);
    }

    public Page<LoginHistoryListDto> myLoginHistories(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return loginHistoryRepository.findByUser(user, pageable)
                .map(LoginHistoryListDto::fromEntity);
    }

    public Page<LoginHistoryListDto> userLoginHistories(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return loginHistoryRepository.findByUser(user, pageable)
                .map(LoginHistoryListDto::fromEntity);
    }

    // 로그인 실패도 이력에 남김
    // user 가 있는 실패만 저장할 때 사용
    public void saveFailure(User user, String ipAddress, String userAgent, String failureReason) {
        LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .failureReason(failureReason)
                .ipAddress(ipAddress)
                .userAgent(userAgent != null ? userAgent : "UNKNOWN")
                .build();

        loginHistoryRepository.save(loginHistory);
    }

    // 최근 성공 로그인 IP 와 현재 IP 가 다른지 확인
    // 첫 성공 로그인 이력이 없으면 새 IP 로 보지 않음
    @Transactional(readOnly = true)
    public boolean requiresIpVerification(User user, String currentIpAddress) {
        return loginHistoryRepository
                .findFirstByUserAndFailureReasonIsNullOrderByLoginHistoryIdDesc(user)
                .map(history -> !history.getIpAddress().equals(currentIpAddress))
                .orElse(false);
    }

    // 기간 조건이 있는 내 로그인 이력 조회
    @Transactional(readOnly = true)
    public Page<LoginHistoryListDto> myLoginHistories(
            Long userId,
            Pageable pageable,
            LocalDate from,
            LocalDate to
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 날짜 input 은 일 단위라서 시작일은 00:00, 종료일은 23:59:59.999999999
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59, 999_999_999) : null;

        // from, to 둘 다 없으면 기존 전체 조회를 사용
        if (fromDateTime == null && toDateTime == null) {
            return loginHistoryRepository.findByUser(user, pageable)
                    .map(LoginHistoryListDto::fromEntity);
        }

        // from, to 둘 다 있으면 기간 사이를 조회
        if (fromDateTime != null && toDateTime != null) {
            return loginHistoryRepository
                    .findByUserAndCreatedAtBetweenOrderByLoginHistoryIdDesc(user, fromDateTime, toDateTime, pageable)
                    .map(LoginHistoryListDto::fromEntity);
        }

        // from 만 있으면 그 이후 이력을 조회
        if (fromDateTime != null) {
            return loginHistoryRepository
                    .findByUserAndCreatedAtGreaterThanEqualOrderByLoginHistoryIdDesc(user, fromDateTime, pageable)
                    .map(LoginHistoryListDto::fromEntity);
        }

        // to 만 있으면 그 이전 이력을 조회
        return loginHistoryRepository
                .findByUserAndCreatedAtLessThanEqualOrderByLoginHistoryIdDesc(user, toDateTime, pageable)
                .map(LoginHistoryListDto::fromEntity);
    }


}

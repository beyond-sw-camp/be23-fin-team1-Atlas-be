package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.SecurityHistory;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.history.SecurityHistoryListDto;
import com.ozz.atlas.auth.repository.SecurityHistoryRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@Transactional
public class SecurityHistoryService {

    private final SecurityHistoryRepository securityHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public SecurityHistoryService(
            SecurityHistoryRepository securityHistoryRepository,
            UserRepository userRepository
    ) {
        this.securityHistoryRepository = securityHistoryRepository;
        this.userRepository = userRepository;
    }

    // 보안 이벤트를 저장
    public void saveHistory(
            User user,
            String eventType,
            String summary,
            String ipAddress,
            String userAgent
    ) {
        SecurityHistory history = SecurityHistory.builder()
                .user(user)
                .eventType(eventType)
                .summary(summary)
                .ipAddress(ipAddress)
                .userAgent(userAgent != null ? userAgent : "UNKNOWN")
                .build();

        securityHistoryRepository.save(history);
    }

    // 현재 로그인한 사용자의 보안 이력을 조회
    @Transactional(readOnly = true)
    public Page<SecurityHistoryListDto> mySecurityHistories(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return securityHistoryRepository.findByUserOrderBySecurityHistoryIdDesc(user, pageable)
                .map(SecurityHistoryListDto::fromEntity);
    }

    // 기간 조건이 있는 내 보안 이력 조회
    @Transactional(readOnly = true)
    public Page<SecurityHistoryListDto> mySecurityHistories(
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
            return securityHistoryRepository.findByUserOrderBySecurityHistoryIdDesc(user, pageable)
                    .map(SecurityHistoryListDto::fromEntity);
        }

        // from, to 둘 다 있으면 기간 사이를 조회
        if (fromDateTime != null && toDateTime != null) {
            return securityHistoryRepository
                    .findByUserAndCreatedAtBetweenOrderBySecurityHistoryIdDesc(user, fromDateTime, toDateTime, pageable)
                    .map(SecurityHistoryListDto::fromEntity);
        }

        // from 만 있으면 그 이후 이력을 조회
        if (fromDateTime != null) {
            return securityHistoryRepository
                    .findByUserAndCreatedAtGreaterThanEqualOrderBySecurityHistoryIdDesc(user, fromDateTime, pageable)
                    .map(SecurityHistoryListDto::fromEntity);
        }

        // to 만 있으면 그 이전 이력을 조회
        return securityHistoryRepository
                .findByUserAndCreatedAtLessThanEqualOrderBySecurityHistoryIdDesc(user, toDateTime, pageable)
                .map(SecurityHistoryListDto::fromEntity);
    }

}

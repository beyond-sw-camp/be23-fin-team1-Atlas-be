package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.SecurityHistory;
import com.ozz.atlas.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SecurityHistoryRepository extends JpaRepository<SecurityHistory, Long> {

    // 특정 사용자의 보안 이력을 최신순으로 조회
    Page<SecurityHistory> findByUserOrderBySecurityHistoryIdDesc(User user, Pageable pageable);

    // 기간 시작일과 종료일이 모두 있을 때 사용
    Page<SecurityHistory> findByUserAndCreatedAtBetweenOrderBySecurityHistoryIdDesc(
            User user,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    // 기간 시작일만 있을 때 사용
    Page<SecurityHistory> findByUserAndCreatedAtGreaterThanEqualOrderBySecurityHistoryIdDesc(
            User user,
            LocalDateTime from,
            Pageable pageable
    );

    // 기간 종료일만 있을 때 사용
    Page<SecurityHistory> findByUserAndCreatedAtLessThanEqualOrderBySecurityHistoryIdDesc(
            User user,
            LocalDateTime to,
            Pageable pageable
    );
}

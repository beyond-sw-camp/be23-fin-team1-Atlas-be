package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.LoginHistory;
import com.ozz.atlas.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    Page<LoginHistory> findByUser(User user, Pageable pageable);

    // 가장 최근의 성공 로그인 이력을 조회
    Optional<LoginHistory> findFirstByUserAndFailureReasonIsNullOrderByLoginHistoryIdDesc(User user);

    // 기간 시작일과 종료일이 모두 있을 때 사용
    Page<LoginHistory> findByUserAndCreatedAtBetweenOrderByLoginHistoryIdDesc(
            User user,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    // 기간 시작일만 있을 때 사용
    Page<LoginHistory> findByUserAndCreatedAtGreaterThanEqualOrderByLoginHistoryIdDesc(
            User user,
            LocalDateTime from,
            Pageable pageable
    );

    // 기간 종료일만 있을 때 사용
    Page<LoginHistory> findByUserAndCreatedAtLessThanEqualOrderByLoginHistoryIdDesc(
            User user,
            LocalDateTime to,
            Pageable pageable
    );
}

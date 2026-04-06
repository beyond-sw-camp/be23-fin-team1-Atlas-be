package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.LoginHistory;
import com.ozz.atlas.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    Page<LoginHistory> findByUser(User user, Pageable pageable);
}
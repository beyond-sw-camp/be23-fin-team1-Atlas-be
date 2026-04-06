package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.LoginHistory;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.LoginHistoryListDto;
import com.ozz.atlas.auth.repository.LoginHistoryRepository;
import com.ozz.atlas.auth.repository.UserRepository;
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

}

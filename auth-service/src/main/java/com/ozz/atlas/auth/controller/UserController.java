package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.service.LoginHistoryService;
import com.ozz.atlas.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final LoginHistoryService loginHistoryService;

    @Autowired
    public UserController(UserService userService, LoginHistoryService loginHistoryService) {
        this.userService = userService;
        this.loginHistoryService = loginHistoryService;
    }

    //    회원가입
    @PostMapping("/users")
    public ResponseEntity<UserCreateResponseDto> signup(@RequestBody @Valid UserSignUpDto dto) {
        String userPublicId = userService.signup(dto);

        UserCreateResponseDto response = UserCreateResponseDto.builder()
                .userPublicId(userPublicId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //사용자 목록 조회
    @GetMapping("/users")
    public ResponseEntity<Page<UserListDto>> userList(
            @PageableDefault(size = 10, sort = "userId", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute UserSearchDto userSearchDto) {

        Page<UserListDto> response = userService.userList(pageable, userSearchDto);
        return ResponseEntity.ok(response);
    }

    //사용자 상세 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailDto> userDetail(@PathVariable Long userId) {
        UserDetailDto response = userService.userDetail(userId);
        return ResponseEntity.ok(response);
    }

    //    내정보 조회
    @GetMapping("/me")
    public ResponseEntity<MyInfoDto> getMyInfo(@AuthenticationPrincipal AuthPrincipal principal) {
        MyInfoDto response = userService.getMyInfo(
                principal.userPublicId(),
                principal.organizationPublicId(),
                principal.role()
        );
        return ResponseEntity.ok(response);
    }

    //    사용자 정보 수정
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserDetailDto> userUpdate(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal) {

        if (!principal.userId().equals(userId) && principal.role() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDetailDto response = userService.userUpdate(userId, dto, principal);
        return ResponseEntity.ok(response);
    }

    //    사용자 삭제
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> userDelete(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthPrincipal principal) {

        if (!principal.userId().equals(userId) && principal.role() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.userDelete(userId, principal);
        return ResponseEntity.noContent().build();
    }

    //    사용자 권한 변경
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserDetailDto> userRoleUpdate(
            @PathVariable Long userId,
            @RequestBody @Valid UserRoleUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal) {

        if (principal.role() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDetailDto response = userService.userRoleUpdate(userId, dto);
        return ResponseEntity.ok(response);
    }

    //비밀번호 변경
    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<Void> userPasswordUpdate(
            @PathVariable Long userId,
            @RequestBody @Valid UserPasswordUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal) {

        if (!principal.userId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.userPasswordUpdate(userId, dto, principal);
        return ResponseEntity.noContent().build();
    }


    //    내 로그인 이력 조회
    @GetMapping("/login-histories/me")
    public ResponseEntity<Page<LoginHistoryListDto>> myLoginHistories(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PageableDefault(size = 10, sort = "loginHistoryId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LoginHistoryListDto> response = loginHistoryService.myLoginHistories(principal.userId(), pageable);
        return ResponseEntity.ok(response);
    }




}

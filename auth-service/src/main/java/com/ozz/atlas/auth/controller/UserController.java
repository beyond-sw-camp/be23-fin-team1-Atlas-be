package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.service.LoginHistoryService;
import com.ozz.atlas.auth.service.SecurityHistoryService;
import com.ozz.atlas.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final LoginHistoryService loginHistoryService;
    private final SecurityHistoryService securityHistoryService;


    @Autowired
    public UserController(UserService userService, LoginHistoryService loginHistoryService, SecurityHistoryService securityHistoryService) {
        this.userService = userService;
        this.loginHistoryService = loginHistoryService;
        this.securityHistoryService = securityHistoryService;
    }

    //    회원가입
    @PostMapping("/users")
    @SecurityRequirements
    @Operation(
            summary = "회원가입",
            description = "조직에 소속된 일반 사용자를 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserSignUpDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "organizationPublicId": "org_01HZX9X5D4P2Q7F8R9S1T2U3V4",
                                              "loginId": "atlas_user01",
                                              "password": "Atlas!234",
                                              "firstName": "Seoyeon",
                                              "middleName": "",
                                              "lastName": "Lee",
                                              "email": "seoyeon.lee@atlas.com",
                                              "phone": "010-8888-9999",
                                              "jobTitle": "Procurement Manager"
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "사용자 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = UserCreateResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "userPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<UserCreateResponseDto> signup(@RequestBody @Valid UserSignUpDto dto) {
        String userPublicId = userService.signup(dto);

        UserCreateResponseDto response = UserCreateResponseDto.builder()
                .userPublicId(userPublicId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 조직 관리자가 자기 조직의 일반 직원 계정을 생성
    @PostMapping("/org-admin/users")
    public ResponseEntity<OrganizationUserCreateResponseDto> createOrganizationUser(
            @RequestBody @Valid OrganizationUserCreateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (principal.role() != UserRole.ORG_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OrganizationUserCreateResponseDto response =
                userService.createOrganizationUser(dto, principal);

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
    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 조직과 역할 정보를 조회한다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = MyInfoDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "organizationPublicId": "org_01HZX9X5D4P2Q7F8R9S1T2U3V4",
                                              "userPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "role": "ADMIN",
                                              "profileAttachmentPublicId": "att_01HZXABCDEF1234567890",
                                              "profileImageThumbPath": "https://atlas-media.s3.ap-northeast-2.amazonaws.com/thumbs/profile.png"
                                            }
                                            """
                            )
                    )
            )
    )
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
    @Operation(
            summary = "사용자 정보 수정",
            description = "기본 프로필 정보와 부서, 프로필 이미지 참조값을 수정한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserUpdateDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "firstName": "철수",
                                              "lastName": "김",
                                              "email": "user01@hanbit.com",
                                              "phone": "010-5555-6666",
                                              "jobTitle": "물류 운영 담당",
                                              "departmentPublicId": "01KQ123456789ABCDEFGHJKMN",
                                              "profileAttachmentPublicId": "att_01HZXABCDEF1234567890",
                                              "profileImageThumbPath": "https://atlas-media.s3.ap-northeast-2.amazonaws.com/thumbs/profile.png"
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = UserDetailDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "userPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "organizationPublicId": "org_01HZX9X5D4P2Q7F8R9S1T2U3V4",
                                              "userId": 1,
                                              "loginId": "atlas_user01",
                                              "firstName": "철수",
                                              "middleName": "",
                                              "lastName": "김",
                                              "email": "user01@hanbit.com",
                                              "phone": "010-5555-6666",
                                              "jobTitle": "물류 운영 담당",
                                              "departmentPublicId": "01KQ123456789ABCDEFGHJKMN",
                                              "departmentCode": "LOGISTICS_DEPARTMENT",
                                              "departmentName": "물류 부서",
                                              "profileAttachmentPublicId": "att_01HZXABCDEF1234567890",
                                              "profileImageThumbPath": "https://atlas-media.s3.ap-northeast-2.amazonaws.com/thumbs/profile.png",
                                              "userRole": "USER"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<UserDetailDto> userUpdate(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal,
            HttpServletRequest request) {

        if (!principal.userId().equals(userId) && principal.role() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 수정 전 사용자 엔티티를 읽어서 변경 요약을 만듭니다.
        User user = userService.getUserEntity(userId);
        String summary = userService.buildProfileUpdateSummary(user, dto);

        UserDetailDto response = userService.userUpdate(userId, dto, principal);

        // 프로필 수정 보안 이력을 저장합니다.
        securityHistoryService.saveHistory(
                user,
                "PROFILE_UPDATED",
                summary,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

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

    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<Void> userPasswordUpdate(
            @PathVariable Long userId,
            @RequestBody @Valid UserPasswordUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal,
            HttpServletRequest request) {

        if (!principal.userId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.userPasswordUpdate(userId, dto, principal);

        // 비밀번호 변경 보안 이력을 저장합니다.
        User user = userService.getUserEntity(userId);

        securityHistoryService.saveHistory(
                user,
                "PASSWORD_CHANGED",
                "비밀번호 변경",
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.noContent().build();
    }


    //    내 로그인 이력 조회
    @GetMapping("/login-histories/me")
    public ResponseEntity<Page<LoginHistoryListDto>> myLoginHistories(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "loginHistoryId", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoginHistoryListDto> response =
                loginHistoryService.myLoginHistories(principal.userId(), pageable, from, to);

        return ResponseEntity.ok(response);
    }


    // 채팅 서비스 등 내부 연동용 사용자 조회
    @GetMapping("/users/public/{userPublicId}")
    @SecurityRequirements
    public ResponseEntity<UserDetailDto> userDetailByPublicId(@PathVariable String userPublicId) {
        UserDetailDto response = userService.userDetailByPublicId(userPublicId);
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 사용자의 보안 이력을 조회
    @GetMapping("/security-histories/me")
    public ResponseEntity<Page<SecurityHistoryListDto>> mySecurityHistories(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "securityHistoryId", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SecurityHistoryListDto> response =
                securityHistoryService.mySecurityHistories(principal.userId(), pageable, from, to);

        return ResponseEntity.ok(response);
    }



}

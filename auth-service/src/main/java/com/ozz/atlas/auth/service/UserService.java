package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.auth.search.service.UserSearchService;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.UUID;



@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSearchService userSearchService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganizationRepository organizationRepository, JwtTokenProvider jwtTokenProvider, UserSearchService userSearchService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userSearchService = userSearchService;
    }

    //    사용자 회원가입
    public String signup(UserSignUpDto dto) {
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }
        Organization organization = organizationRepository.findByPublicId(dto.getOrganizationPublicId()).
                orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직 입니다."));

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(organization, encodedPassword);

        User savedUser = userRepository.save(user);
        userSearchService.saveUserDocument(savedUser);
        return savedUser.getPublicId();
    }
    // 관리자가 조직의 최초 ORG_ADMIN 계정을 생성
    // 임시 비밀번호는 서버가 생성해서 응답으로 내려줌
    public InitialOrgAdminCreateResponseDto createInitialOrgAdmin(
            String organizationPublicId,
            InitialOrgAdminCreateDto dto
    ) {
        // 이미 쓰는 로그인 ID면 생성할 수 없음
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID입니다.");
        }

        // 조직이 없으면 대표자 계정을 만들 수 없음
        Organization organization = organizationRepository.findByPublicId(organizationPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));

        // 해당 조직에 활성 상태 ORG_ADMIN 이 이미 있으면 최초 관리자 생성을 막음
        boolean orgAdminExists = userRepository.existsByOrganization_PublicIdAndUserRoleAndStatus(
                organizationPublicId,
                UserRole.ORG_ADMIN,
                Status.ACTIVE
        );

        if (orgAdminExists) {
            throw new IllegalArgumentException("해당 조직에는 이미 대표자 계정이 존재합니다.");
        }

        // 최초 로그인용 임시 비밀번호를 생성
        String temporaryPassword = createTemporaryPassword();

        // DB에는 암호화된 비밀번호를 저장
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        // 최초 대표자 계정을 생성
        User user = User.builder()
                .organization(organization)
                .loginId(dto.getLoginId())
                .password(encodedPassword)
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .jobTitle(dto.getJobTitle())
                .userRole(UserRole.ORG_ADMIN)
                .status(Status.ACTIVE)
                .passwordChangeRequired(true)
                .build();

        User savedUser = userRepository.save(user);
        userSearchService.saveUserDocument(savedUser);

        return InitialOrgAdminCreateResponseDto.builder()
                .userPublicId(savedUser.getPublicId())
                .organizationPublicId(organization.getPublicId())
                .temporaryPassword(temporaryPassword)
                .passwordChangeRequired(true)
                .build();
    }

    // 조직 관리자가 자기 조직의 일반 직원 계정을 생성
    // 조직 정보는 로그인한 ORG_ADMIN 의 토큰에서 가져
    public OrganizationUserCreateResponseDto createOrganizationUser(
            OrganizationUserCreateDto dto,
            AuthPrincipal principal
    ) {
        // ORG_ADMIN 만 직원 계정을 만들 수 있음
        if (principal.role() != UserRole.ORG_ADMIN) {
            throw new IllegalArgumentException("직원 계정 생성 권한이 없습니다.");
        }

        // 이미 쓰는 로그인 ID면 생성할 수 없음
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID입니다.");
        }

        // 현재 로그인한 대표자의 조직을 기준으로 직원을 생성
        Organization organization = organizationRepository.findByPublicId(principal.organizationPublicId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));

        // 직원 첫 로그인용 임시 비밀번호를 생성
        String temporaryPassword = createTemporaryPassword();

        // DB에는 암호화된 비밀번호를 저장
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        // 일반 직원(USER) 계정을 생성
        User user = User.builder()
                .organization(organization)
                .loginId(dto.getLoginId())
                .password(encodedPassword)
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .jobTitle(dto.getJobTitle())
                .userRole(UserRole.USER)
                .status(Status.ACTIVE)
                .passwordChangeRequired(true)
                .build();

        User savedUser = userRepository.save(user);
        userSearchService.saveUserDocument(savedUser);

        return OrganizationUserCreateResponseDto.builder()
                .userPublicId(savedUser.getPublicId())
                .organizationPublicId(organization.getPublicId())
                .temporaryPassword(temporaryPassword)
                .passwordChangeRequired(true)
                .build();
    }



    //    사용자 정보 조회 (내정보조회)
    public MyInfoDto getMyInfo(String userPublicId, String organizationPublicId, UserRole role) {
        return MyInfoDto.builder()
                .userPublicId(userPublicId)
                .organizationPublicId(organizationPublicId)
                .role(role)
                .build();

    }

    //    사용자 목록 조회
    public Page<UserListDto> userList(Pageable pageable, UserSearchDto searchDto) {
        // 검색 조건이 하나라도 있으면 Elasticsearch 통합검색을 사용
        // 조건이 전혀 없을 때만 기본 DB 목록 조회로 내려감
        if (hasSearchCondition(searchDto)) {
            return userSearchService.search(pageable, searchDto);
        }

        return userRepository.findAll(pageable)
                .map(UserListDto::fromEntity);
    }



    //    사용자 상세 조회
    public UserDetailDto userDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return UserDetailDto.fromEntity(user);
    }


    //    사용자 정보 수정
    public UserDetailDto userUpdate(Long userId, UserUpdateDto dto, AuthPrincipal principal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (!principal.userId().equals(userId) && principal.role() != UserRole.ADMIN) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        user.updateUser(dto);
        userSearchService.saveUserDocument(user);

        return UserDetailDto.fromEntity(user);
    }

    //    사용자 삭제
    public void userDelete(Long userId, AuthPrincipal principal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (!principal.userId().equals(userId) && principal.role() != UserRole.ADMIN) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        user.deleteUser();
        userSearchService.saveUserDocument(user);
    }

    // 사용자 권한 변경
    public UserDetailDto userRoleUpdate(Long userId, UserRoleUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        user.updateUserRole(dto.getUserRole());
        userSearchService.saveUserDocument(user);

        return UserDetailDto.fromEntity(user);
    }

    //    사용자 비밀번호 변경
    public void userPasswordUpdate(Long userId, UserPasswordUpdateDto dto, AuthPrincipal principal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (!principal.userId().equals(userId)) {
            throw new IllegalArgumentException("비밀번호 변경 권한이 없습니다.");
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호와 다른 비밀번호를 입력해주세요.");
        }

        user.updatePassword(passwordEncoder.encode(dto.getNewPassword()));

        // 비밀번호를 정상적으로 바꿨으므로 강제 변경 상태를 해제합니다.
        user.clearPasswordChangeRequired();

        jwtTokenProvider.revokeRefreshToken(userId);

    }


    // 검색 DTO에 실제 검색 조건이 하나라도 들어왔는지 확인
    // keyword 뿐 아니라 조직, 권한, 이름, 아이디, 상태도 모두 통합검색 진입 조건으로 봄
    private boolean hasSearchCondition(UserSearchDto searchDto) {
        if (searchDto == null) {
            return false;
        }

        return hasText(searchDto.getOrganizationPublicId())
                || searchDto.getUserRole() != null
                || hasText(searchDto.getLoginId())
                || hasText(searchDto.getFirstName())
                || hasText(searchDto.getMiddleName())
                || hasText(searchDto.getLastName())
                || searchDto.getStatus() != null
                || hasText(searchDto.getKeyword());
    }

    // 문자열 값이 null 이거나 공백인지 확인하는 공통 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // userPublicId 기준 사용자 상세 조회
    public UserDetailDto userDetailByPublicId(String userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return UserDetailDto.fromEntity(user);
    }
    // 임시 비밀번호를 간단히 생성
    // 나중에 규칙이 필요하면 별도 유틸로 분리할 수 있음
    private String createTemporaryPassword() {
        return "Atlas!" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }




}

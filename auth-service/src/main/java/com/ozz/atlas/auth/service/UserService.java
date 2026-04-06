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
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

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
        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isBlank()) {
            return userSearchService.search(pageable, searchDto);
        }
        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchDto.getOrganizationPublicId() != null && !searchDto.getOrganizationPublicId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("organization").get("publicId"), searchDto.getOrganizationPublicId()));
            }

            if (searchDto.getUserRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userRole"), searchDto.getUserRole()));
            }

            if (searchDto.getLoginId() != null && !searchDto.getLoginId().isBlank()) {
                predicates.add(criteriaBuilder.like(root.get("loginId"), "%" + searchDto.getLoginId() + "%"));
            }

            if (searchDto.getFirstName() != null && !searchDto.getFirstName().isBlank()) {
                predicates.add(criteriaBuilder.like(root.get("firstName"), "%" + searchDto.getFirstName() + "%"));
            }

            if (searchDto.getMiddleName() != null && !searchDto.getMiddleName().isBlank()) {
                predicates.add(criteriaBuilder.like(root.get("middleName"), "%" + searchDto.getMiddleName() + "%"));
            }

            if (searchDto.getLastName() != null && !searchDto.getLastName().isBlank()) {
                predicates.add(criteriaBuilder.like(root.get("lastName"), "%" + searchDto.getLastName() + "%"));
            }

            if (searchDto.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchDto.getStatus()));
            } else {
                predicates.add(criteriaBuilder.equal(root.get("status"), Status.ACTIVE));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(specification, pageable)
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
        jwtTokenProvider.revokeRefreshToken(userId);
    }

}

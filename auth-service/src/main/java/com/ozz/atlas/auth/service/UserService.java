package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
    }
//    사용자 회원가입
    public String signup(UserSignUpDto dto){
        if (userRepository.existsByLoginId(dto.getLoginId())){
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }
        Organization organization = organizationRepository.findByPublicId(dto.getOrganizationPublicId()).
                orElseThrow(()-> new IllegalArgumentException("존재하지 않는 조직 입니다."));

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(organization, encodedPassword);

        User savedUser = userRepository.save(user);
        return savedUser.getPublicId();
    }
//    사용자 정보 조회 (내정보조회)
    public MyInfoDto getMyInfo(String userPublicId, String organizationPublicId, UserRole role){
        return MyInfoDto.builder()
                .userPublicId(userPublicId)
                .organizationPublicId(organizationPublicId)
                .role(role)
                .build();

    }
//    사용자 목록 조회
public Page<UserListDto> userList(Pageable pageable, UserSearchDto searchDto) {
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
        }

        return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };

    return userRepository.findAll(specification, pageable)
            .map(UserListDto::fromEntity);
}
//    사용자 상세 조회
    public UserDetailDto userDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return UserDetailDto.fromEntity(user);
    }

}

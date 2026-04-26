package com.ozz.atlas.auth.domain;

import com.ozz.atlas.auth.dtos.user.UserUpdateDto;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String firstName;

    private String middleName;

    @Column(nullable = false, length = 10)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 50)
    private String jobTitle;

    @Column(length = 26)
    private String profileAttachmentPublicId;

    @Column(length = 255)
    private String profileImageThumbPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, length = 40)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column
    private LocalDateTime passwordChangedAt;

    // 임시 비밀번호로 만든 계정인지 표시합니다.
    // true면 첫 로그인 뒤 비밀번호를 다시 설정해야 합니다.
    @Builder.Default
    @Column(nullable = false)
    private boolean passwordChangeRequired = false;

    public void updateUser(UserUpdateDto dto) {
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            this.firstName = dto.getFirstName();
        }
        if (dto.getMiddleName() != null) {
            this.middleName = dto.getMiddleName();
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            this.lastName = dto.getLastName();
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            this.email = dto.getEmail();
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            this.phone = dto.getPhone();
        }
        if (dto.getJobTitle() != null) {
            this.jobTitle = dto.getJobTitle();
        }
        if (dto.getProfileAttachmentPublicId() != null) {
            this.profileAttachmentPublicId = dto.getProfileAttachmentPublicId();
        }
        if (dto.getProfileImageThumbPath() != null) {
            this.profileImageThumbPath = dto.getProfileImageThumbPath();
        }
    }

    public void updateDepartment(Department department) {
        this.department = department;
    }

    public String getProfileAttachmentPublicId() {
        return profileAttachmentPublicId;
    }

    public String getProfileImageThumbPath() {
        return profileImageThumbPath;
    }

    // 사용자의 상태를 원하는 값으로 바꿉니다.
    // 삭제도 별도 메서드 대신 이 메서드로 통일해서 처리합니다.
    public void changeStatus(Status status) {
        this.status = status;
    }

    public void updateUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
    }

    // 최초 생성 계정처럼 비밀번호 변경을 강제해야 할 때 호출합니다.
    public void requirePasswordChange() {
        this.passwordChangeRequired = true;
    }

    // 비밀번호를 정상적으로 바꾼 뒤에는 강제 변경 상태를 해제합니다.
    public void clearPasswordChangeRequired() {
        this.passwordChangeRequired = false;
    }
}

package com.ozz.atlas.auth.domain;
import com.ozz.atlas.auth.dtos.UserUpdateDto;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import com.ozz.atlas.common.jpa.Status;

import java.time.LocalDateTime;


@Getter @ToString
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, length = 40)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column
    private LocalDateTime passwordChangedAt;

    public void updateUser(UserUpdateDto dto) {
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) this.firstName = dto.getFirstName();
        if (dto.getMiddleName() != null) this.middleName = dto.getMiddleName();
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) this.lastName = dto.getLastName();
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) this.email = dto.getEmail();
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) this.phone = dto.getPhone();
        if (dto.getJobTitle() != null) this.jobTitle = dto.getJobTitle();
    }

    public void deleteUser() {
        this.status = Status.DELETE;
    }

    public void updateUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
    }



}

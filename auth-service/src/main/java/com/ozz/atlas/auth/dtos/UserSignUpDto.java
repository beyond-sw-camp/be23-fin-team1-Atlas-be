package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.common.jpa.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSignUpDto {

    @NotBlank(message = "조직 식별값은 비어있으면 안 됩니다.")
    private String organizationPublicId;

    @NotBlank(message = "ID가 비어있으면 안 됩니다.")
    private String loginId;

    @NotBlank(message = "비밀번호가 비어있으면 안 됩니다.")
    private String password;

    @NotBlank(message = "이름이 비어있으면 안 됩니다.")
    private String firstName;
    private String middleName;
    @NotBlank(message = "성이 비어있으면 안 됩니다.")
    private String lastName;

    @NotBlank(message = "email이 비어있으면 안 됩니다.")
    private String email;

    @NotBlank(message = "연락처가 비어있으면 안 됩니다.")
    private String phone;

    private String jobTitle;

    private String profileImagePublicId;

    private Status status;

    public User toEntity(Organization organization, String encodedPassword) {
        return User.builder()
                .organization(organization)
                .loginId(this.loginId)
                .password(encodedPassword)
                .firstName(this.firstName)
                .middleName(this.middleName)
                .lastName(this.lastName)
                .email(this.email)
                .phone(this.phone)
                .jobTitle(this.jobTitle)
                .userRole(UserRole.USER)
                .status(Status.ACTIVE)
                .build();
    }



}

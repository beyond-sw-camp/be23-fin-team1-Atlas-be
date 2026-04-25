package com.ozz.atlas.auth.dtos.user;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.search.document.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserListDto {
    private String userPublicId;
    private String organizationPublicId;
    private String loginId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
    private String departmentPublicId;
    private String departmentCode;
    private String departmentName;
    private UserRole userRole;

    public static UserListDto fromEntity(User user){
        return UserListDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganization().getPublicId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .departmentPublicId(user.getDepartment() != null ? user.getDepartment().getPublicId() : null)
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null)
                .userRole(user.getUserRole())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .build();
    }

    public static UserListDto fromDocument(UserDocument user) {
        return UserListDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganizationPublicId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .departmentPublicId(user.getDepartmentPublicId())
                .departmentCode(user.getDepartmentCode())
                .departmentName(user.getDepartmentName())
                .build();
    }
}

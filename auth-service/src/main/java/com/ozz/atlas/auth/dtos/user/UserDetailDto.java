package com.ozz.atlas.auth.dtos.user;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailDto {
    private String userPublicId;
    private String organizationPublicId;
    private Long userId;
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
    private String profileAttachmentPublicId;
    private String profileImageThumbPath;
    private UserRole userRole;


    public static UserDetailDto fromEntity(User user) {
        return UserDetailDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganization().getPublicId())
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .departmentPublicId(user.getDepartment() != null ? user.getDepartment().getPublicId() : null)
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null)
                .profileAttachmentPublicId(user.getProfileAttachmentPublicId())
                .profileImageThumbPath(user.getProfileImageThumbPath())
                .userRole(user.getUserRole())
                .build();
    }
}

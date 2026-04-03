package com.ozz.atlas.auth.dtos;

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
                .userRole(user.getUserRole())
                .build();
    }
}

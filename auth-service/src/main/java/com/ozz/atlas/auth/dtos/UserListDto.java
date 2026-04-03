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
    private UserRole userRole;

    public static UserListDto fromEntity(User user){
        return UserListDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganization().getPublicId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .userRole(user.getUserRole())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .build();
    }
}

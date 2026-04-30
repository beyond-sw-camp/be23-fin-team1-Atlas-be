package com.ozz.atlas.control.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthUserDetailDto {

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

    private String profileImageThumbPath;
}

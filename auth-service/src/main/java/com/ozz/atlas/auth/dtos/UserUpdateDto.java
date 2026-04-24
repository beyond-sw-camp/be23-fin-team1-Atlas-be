package com.ozz.atlas.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
    private String departmentPublicId;

}

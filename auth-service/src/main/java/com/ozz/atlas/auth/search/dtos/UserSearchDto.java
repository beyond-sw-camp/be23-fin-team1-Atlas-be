package com.ozz.atlas.auth.search.dtos;

import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSearchDto {
    private String organizationPublicId;
    private UserRole userRole;
    private String loginId;
    private String firstName;
    private String middleName;
    private String lastName;
    private Status status;
    private String keyword;
}

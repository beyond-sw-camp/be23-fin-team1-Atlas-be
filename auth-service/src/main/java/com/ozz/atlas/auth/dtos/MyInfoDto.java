package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyInfoDto {
    private String organizationPublicId;
    private String userPublicId;
    private UserRole role;
}

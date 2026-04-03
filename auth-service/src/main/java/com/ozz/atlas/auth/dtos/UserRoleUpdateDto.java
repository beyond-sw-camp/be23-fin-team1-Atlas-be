package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRoleUpdateDto {
    @NotNull(message = "변경할 권한은 필수입니다.")
    private UserRole userRole;
}

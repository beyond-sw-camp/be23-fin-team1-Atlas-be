package com.ozz.atlas.auth.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "날짜 모델")
public class UserRoleUpdateDto {
    @NotNull(message = "변경할 권한은 필수입니다.")
    @Schema(description = "user Role 값", example = "sample")
    private UserRole userRole;
}

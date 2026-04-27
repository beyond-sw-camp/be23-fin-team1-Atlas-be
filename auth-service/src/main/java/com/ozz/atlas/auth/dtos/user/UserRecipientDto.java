package com.ozz.atlas.auth.dtos.user;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "내부 알림 수신자 응답")
public record UserRecipientDto(
        @Schema(description = "사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
        String userPublicId,
        @Schema(description = "소속 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
        String organizationPublicId,
        @Schema(description = "부서 코드", example = "LOGISTICS_DEPARTMENT", nullable = true)
        String departmentCode,
        @Schema(description = "사용자 권한", example = "USER")
        UserRole userRole
) {

    public static UserRecipientDto fromEntity(User user) {
        return UserRecipientDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganization().getPublicId())
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null)
                .userRole(user.getUserRole())
                .build();
    }
}

package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "내 정보 요약 응답")
public class MyInfoDto {
    @Schema(description = "소속 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;

    @Schema(description = "사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String userPublicId;

    @Schema(description = "사용자 역할", example = "ADMIN")
    private UserRole role;
}

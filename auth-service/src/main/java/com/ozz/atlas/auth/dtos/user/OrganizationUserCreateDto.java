package com.ozz.atlas.auth.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 조직 초기 관리자 생성,
// 조직 일반 사원 생성
// 둘 다 공통으로 쓰는 요청 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "자동 생성 사용자 계정 생성 요청")
public class OrganizationUserCreateDto {

    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Schema(description = "이름", example = "철수")
    private String firstName;

    @Schema(description = "중간이름", example = "")
    private String middleName;

    @NotBlank(message = "성은 비어 있을 수 없습니다.")
    @Schema(description = "성", example = "김")
    private String lastName;

    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @Schema(description = "이메일", example = "user01@hanbit.com")
    private String email;

    @NotBlank(message = "연락처는 비어 있을 수 없습니다.")
    @Schema(description = "전화번호", example = "010-5555-6666")
    private String phone;

    @Schema(description = "직책", example = "물류 운영 담당")
    private String jobTitle;

    // 부서 공개 ID
    // 초기 ORG_ADMIN 생성에서는 없을 수 있고,
    // 일반 사원 생성에서는 서비스에서 필수로 검사
    @Schema(description = "부서 공개 ID", example = "01KQ123456789ABCDEFGHJKMN")
    private String departmentPublicId;
}

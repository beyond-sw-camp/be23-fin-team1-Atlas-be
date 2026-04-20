package com.ozz.atlas.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "조직 관리자의 직원 계정 생성 요청")
public class OrganizationUserCreateDto {

    @NotBlank(message = "로그인 ID는 비어 있을 수 없습니다.")
    @Schema(description = "직원 로그인 ID", example = "hanbit_user01")
    private String loginId;

    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Schema(description = "이름", example = "철수")
    private String firstName;

    @Schema(description = "미들네임", example = "")
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

    @Schema(description = "직책", example = "품질 담당자")
    private String jobTitle;
}

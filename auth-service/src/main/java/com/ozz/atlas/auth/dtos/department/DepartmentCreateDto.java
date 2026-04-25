package com.ozz.atlas.auth.dtos.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "부서 생성 요청")
public class DepartmentCreateDto {

    @NotBlank(message = "부서 코드는 비어 있을 수 없습니다.")
    @Pattern(regexp = "^[A-Z0-9_]{2,50}$", message = "부서 코드는 영문 대문자, 숫자, 언더스코어만 사용할 수 있습니다.")
    @Schema(description = "부서 코드", example = "LOGISTICS_DEPARTMENT")
    private String departmentCode;

    @NotBlank(message = "부서명은 비어 있을 수 없습니다.")
    @Schema(description = "부서명", example = "물류 부서")
    private String departmentName;
}

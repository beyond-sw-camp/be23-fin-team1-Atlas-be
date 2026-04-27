package com.ozz.atlas.auth.dtos.department;

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
@Schema(description = "부서 수정 요청")
public class DepartmentUpdateDto {

    @NotBlank(message = "부서명은 비어 있을 수 없습니다.")
    @Schema(description = "부서명", example = "품질 부서")
    private String departmentName;
}

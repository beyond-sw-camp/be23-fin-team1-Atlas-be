package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.Department;
import com.ozz.atlas.common.jpa.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "부서 응답")
public class DepartmentResponseDto {

    @Schema(description = "부서 공개 식별자", example = "01KQ123456789ABCDEFGHJKMN")
    private String departmentPublicId;

    @Schema(description = "부서 코드", example = "LOGISTICS_DEPARTMENT")
    private String departmentCode;

    @Schema(description = "부서명", example = "물류 부서")
    private String departmentName;

    @Schema(description = "부서 상태", example = "ACTIVE")
    private Status status;

    public static DepartmentResponseDto fromEntity(Department department) {
        return DepartmentResponseDto.builder()
                .departmentPublicId(department.getPublicId())
                .departmentCode(department.getDepartmentCode())
                .departmentName(department.getDepartmentName())
                .status(department.getStatus())
                .build();
    }
}

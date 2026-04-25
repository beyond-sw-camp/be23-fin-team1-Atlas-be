package com.ozz.atlas.auth.dtos.department;

import com.ozz.atlas.common.jpa.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "부서 상태 변경 요청")
public class DepartmentStatusUpdateDto {

    @NotNull(message = "부서 상태는 비어 있을 수 없습니다.")
    @Schema(description = "부서 상태", example = "ACTIVE")
    private Status status;
}

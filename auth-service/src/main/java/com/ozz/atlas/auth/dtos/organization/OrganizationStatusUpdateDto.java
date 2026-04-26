package com.ozz.atlas.auth.dtos.organization;

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
@Schema(description = "조직 상태 변경 요청")
public class OrganizationStatusUpdateDto {

    // 조직을 활성, 비활성, 삭제 상태 중 하나로 바꿉니다.
    @NotNull(message = "조직 상태는 필수입니다.")
    @Schema(description = "조직 상태", example = "DEACTIVE")
    private Status status;
}

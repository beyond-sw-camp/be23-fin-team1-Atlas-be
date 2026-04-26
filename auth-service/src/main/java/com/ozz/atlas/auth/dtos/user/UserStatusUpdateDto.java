package com.ozz.atlas.auth.dtos.user;

import com.ozz.atlas.common.jpa.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 사용자를 활성, 비활성, 삭제 상태 중 하나로 바꿀 때 사용하는 요청 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "사용자 상태 변경 요청")
public class UserStatusUpdateDto {

    // 바꿀 사용자 상태
    @NotNull(message = "사용자 상태는 필수입니다.")
    @Schema(description = "변경할 사용자 상태", example = "DEACTIVE")
    private Status status;
}

package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상태 요청")
public class ChangePurchaseOrderStatusRequest {

    // 이 엔드포인트는 buyer가 직접 상태를 바꾸는 용도라
    // CANCELLED, COMPLETED 정도만 허용하는 방향으로 service에서 막는다.
    @NotNull
    @Schema(description = "상태", example = "ACTIVE")
    private PoStatus poStatus;
}

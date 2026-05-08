package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "날짜 요청")
public class UpdatePurchaseOrderRequest {
    @Schema(description = "메모", example = "샘플 내용", nullable = true)
    private String memo;
}

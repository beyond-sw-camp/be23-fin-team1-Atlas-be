package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.purchaseorder.domain.CurrencyCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Purchase Order Batch 값 요청")
public class CreatePurchaseOrderBatchRequest {

    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private CurrencyCode currencyCode;

    @Size(max = 1000)
    @Schema(description = "메모", example = "샘플 내용", nullable = true)
    private String memo;

    @Valid
    @NotEmpty
    @Schema(description = "lines 값")
    private List<CreatePurchaseOrderBatchLineRequest> lines;
}

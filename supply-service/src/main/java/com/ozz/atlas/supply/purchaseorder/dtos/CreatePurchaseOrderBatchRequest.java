package com.ozz.atlas.supply.purchaseorder.dtos;

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
public class CreatePurchaseOrderBatchRequest {

    private CurrencyCode currencyCode;

    @Size(max = 1000)
    private String memo;

    @Valid
    @NotEmpty
    private List<CreatePurchaseOrderBatchLineRequest> lines;
}

package com.ozz.atlas.supply.purchaseorder.dtos;

import com.ozz.atlas.supply.purchaseorder.domain.CurrencyCode;
import com.ozz.atlas.supply.purchaseorder.domain.PriorityCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {

    @NotBlank
    @Size(max = 50)
    private String poNumber;

    @NotBlank
    @Size(max = 26)
    private String supplierPublicId;

    private PriorityCode priorityCode; // 긴급도

    @NotNull
    @FutureOrPresent
    private LocalDate dueDate; // 납기일

    private CurrencyCode currencyCode;

    @Size(max = 1000)
    private String memo;

    @Valid
    @NotEmpty
    private List<CreatePurchaseOrderItemRequest> items;
}

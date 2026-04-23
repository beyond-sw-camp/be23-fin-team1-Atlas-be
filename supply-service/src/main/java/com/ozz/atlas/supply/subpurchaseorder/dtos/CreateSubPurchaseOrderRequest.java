package com.ozz.atlas.supply.subpurchaseorder.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateSubPurchaseOrderRequest {

    @NotBlank
    private String parentPoPublicId;

    @NotBlank
    private String supplierPublicId;

    @Valid
    @NotEmpty
    private List<CreateSubPurchaseOrderItemRequest> items;
}

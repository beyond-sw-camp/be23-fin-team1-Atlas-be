package com.ozz.atlas.supply.subpurchaseorder.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubPurchaseOrderBatchRequest {

    @NotBlank
    private String parentPoPublicId;

    @Valid
    @NotEmpty
    private List<CreateSubPurchaseOrderBatchLineRequest> lines;
}

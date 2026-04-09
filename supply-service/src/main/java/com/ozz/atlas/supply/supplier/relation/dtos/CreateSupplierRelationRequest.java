package com.ozz.atlas.supply.supplier.relation.dtos;

import com.ozz.atlas.supply.supplier.relation.domain.SupplierRelationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateSupplierRelationRequest {

    @NotBlank
    private String childSupplierPublicId;

    @NotNull
    private SupplierRelationType relationType;

    @NotNull
    @Min(1)
    private Integer priorityRank;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}

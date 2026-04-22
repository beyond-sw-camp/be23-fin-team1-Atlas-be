package com.ozz.atlas.supply.supplier.relation.dtos;

import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateSupplierRelationRequest {

    @Min(1)
    private Integer priorityRank;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}

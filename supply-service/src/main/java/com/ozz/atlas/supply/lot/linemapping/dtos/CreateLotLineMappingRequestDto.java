package com.ozz.atlas.supply.lot.linemapping.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLotLineMappingRequestDto {

    @NotNull
    private Long productionLineId;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal processedQty;

    private String mappingNote;

}

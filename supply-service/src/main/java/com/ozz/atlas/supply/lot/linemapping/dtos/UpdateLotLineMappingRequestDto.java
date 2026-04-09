package com.ozz.atlas.supply.lot.linemapping.dtos;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateLotLineMappingRequestDto {

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal processedQty;

    private String mappingNote;
}

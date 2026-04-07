package com.ozz.atlas.supply.productionline.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductionLineUpdateDto {

    @NotBlank
    @Size(max = 50)
    private String lineCode;

    @NotBlank
    @Size(max = 100)
    private String lineName;

    @Size(max = 30)
    private String lineType;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal dailyCapacity;


}

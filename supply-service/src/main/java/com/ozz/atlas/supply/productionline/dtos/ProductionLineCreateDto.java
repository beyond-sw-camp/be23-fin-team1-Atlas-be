package com.ozz.atlas.supply.productionline.dtos;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
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
public class ProductionLineCreateDto {

    @NotBlank
    @Size(max = 26)
    private String logisticsNodePublicId;

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

    public ProductionLine toEntity() {
        return ProductionLine.builder()
                .logisticsNodePublicId(logisticsNodePublicId)
                .lineCode(lineCode)
                .lineName(lineName)
                .lineType(lineType)
                .status(Status.ACTIVE)
                .dailyCapacity(dailyCapacity)
                .build();
    }
}

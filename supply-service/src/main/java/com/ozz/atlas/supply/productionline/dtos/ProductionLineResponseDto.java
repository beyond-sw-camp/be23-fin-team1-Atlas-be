package com.ozz.atlas.supply.productionline.dtos;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductionLineResponseDto {
    private Long productionLineId;
    private String logisticsNodePublicId;
    private String lineCode;
    private String lineName;
    private String lineType;
    private Status status;
    private BigDecimal dailyCapacity;

    public static ProductionLineResponseDto fromEntity(ProductionLine productionLine) {
        return ProductionLineResponseDto.builder()
                .productionLineId(productionLine.getProductionLineId())
                .logisticsNodePublicId(productionLine.getLogisticsNodePublicId())
                .lineCode(productionLine.getLineCode())
                .lineName(productionLine.getLineName())
                .lineType(productionLine.getLineType())
                .status(productionLine.getStatus())
                .dailyCapacity(productionLine.getDailyCapacity())
                .build();
    }
}

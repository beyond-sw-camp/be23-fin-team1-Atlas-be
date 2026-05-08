package com.ozz.atlas.supply.productionline.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Production Line 값 응답")
public class ProductionLineResponseDto {
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long productionLineId;
    @Schema(description = "물류 노드 공개 식별자", example = "sample_public_id", nullable = true)
    private String logisticsNodePublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String lineCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String lineName;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String lineType;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private Status status;
    @Schema(description = "daily Capacity 값", example = "1", nullable = true)
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

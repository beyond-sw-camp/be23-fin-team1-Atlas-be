package com.ozz.atlas.supply.productionline.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Production Line Create 값 모델")
public class ProductionLineCreateDto {

    @NotBlank
    @Size(max = 26)
    @Schema(description = "물류 노드 공개 식별자", example = "sample_public_id")
    private String logisticsNodePublicId;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "코드", example = "CODE-001")
    private String lineCode;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "이름", example = "샘플 이름")
    private String lineName;

    @Size(max = 30)
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String lineType;

    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "daily Capacity 값", example = "1", nullable = true)
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

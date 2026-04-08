package com.ozz.atlas.supply.lot.linemapping.dtos;

import com.ozz.atlas.supply.lot.linemapping.domain.LotLineMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LotLineMappingResponseDto {

    private Long lotLineMappingId;

    private String lotPublicId;

    private Long productionLineId;

    private String lineCode;

    private String lineName;

    private BigDecimal processedQty;

    private LocalDateTime processStartedAt;

    private LocalDateTime processEndedAt;

    private String mappingNote;


    public static LotLineMappingResponseDto fromEntity(LotLineMapping lotLineMapping){
        return LotLineMappingResponseDto.builder()
                .lotLineMappingId(lotLineMapping.getLotLineMappingId())
                .lotPublicId(lotLineMapping.getLotPublicId())
                .productionLineId(lotLineMapping.getProductionLine().getProductionLineId())
                .lineCode(lotLineMapping.getProductionLine().getLineCode())
                .lineName(lotLineMapping.getProductionLine().getLineName())
                .processedQty(lotLineMapping.getProcessedQty())
                .processStartedAt(lotLineMapping.getProcessStartedAt())
                .processEndedAt(lotLineMapping.getProcessEndedAt())
                .mappingNote(lotLineMapping.getMappingNote())
                .build();
    }

}

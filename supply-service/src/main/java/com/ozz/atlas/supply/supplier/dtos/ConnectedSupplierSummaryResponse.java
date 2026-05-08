package com.ozz.atlas.supply.supplier.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Connected Supplier Summary 값 응답")
public class ConnectedSupplierSummaryResponse {

    @Schema(description = "개수", example = "1", nullable = true)
    private Long connectedSupplierCount;
    @Schema(description = "average On Time Rate 값", example = "2026-05-08T10:00:00", nullable = true)
    private BigDecimal averageOnTimeRate;
    @Schema(description = "average Lead Time Days 값", example = "2026-05-08T10:00:00", nullable = true)
    private Integer averageLeadTimeDays;
    public static ConnectedSupplierSummaryResponse of(
            Long connectedSupplierCount,
            BigDecimal averageOnTimeRate,
            Integer averageLeadTimeDays
    ) {
        return ConnectedSupplierSummaryResponse.builder()
                .connectedSupplierCount(connectedSupplierCount != null ? connectedSupplierCount : 0L)
                .averageOnTimeRate(averageOnTimeRate)
                .averageLeadTimeDays(averageLeadTimeDays != null ? averageLeadTimeDays : 0)
                .build();
    }
}

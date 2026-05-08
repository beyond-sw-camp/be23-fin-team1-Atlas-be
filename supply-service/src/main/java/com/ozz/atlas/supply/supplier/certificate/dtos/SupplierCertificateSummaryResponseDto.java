package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Supplier Certificate Summary 값 응답")
public class SupplierCertificateSummaryResponseDto {
    @Schema(description = "식별자", example = "1", nullable = true)
    private long validCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private long expiringSoonCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private long renewalNeededCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private long totalCount;
}

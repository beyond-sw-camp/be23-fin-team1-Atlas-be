package com.ozz.atlas.supply.supplier.certificate.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupplierCertificateSummaryResponseDto {
    private long validCount;
    private long expiringSoonCount;
    private long renewalNeededCount;
    private long totalCount;
}

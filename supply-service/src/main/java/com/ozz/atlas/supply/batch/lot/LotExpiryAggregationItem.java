package com.ozz.atlas.supply.batch.lot;

import com.ozz.atlas.supply.batch.domain.LotExpiryBucket;

import java.math.BigDecimal;
import java.time.LocalDate;

// LOT 1건
public record LotExpiryAggregationItem(
        LocalDate aggregationDate,
        String supplierPublicId,
        String itemPublicId,
        LotExpiryBucket bucket,
        BigDecimal qty
) {
}

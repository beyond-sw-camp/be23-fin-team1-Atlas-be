package com.ozz.atlas.supply.batch.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// LOT 유통기한 집계 결과 저장
@Getter
@Entity
@Table(
        name = "lot_expiry_aggregation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lot_expiry_agg_date_supplier_item_bucket",
                        columnNames = {"aggregation_date", "supplier_public_id", "item_public_id", "bucket"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LotExpiryAggregation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregation_date", nullable = false)
    private LocalDate aggregationDate; // 언제 집계한 결과인지

    @Column(name = "supplier_public_id", nullable = false, length = 26)
    private String supplierPublicId;

    @Column(name = "item_public_id", nullable = false, length = 26)
    private String itemPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotExpiryBucket bucket;

    @Column(name = "lot_count", nullable = false)
    private Long lotCount;

    @Column(name = "total_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal totalQty;

    public LotExpiryAggregation(
            LocalDate aggregationDate,
            String supplierPublicId,
            String itemPublicId,
            LotExpiryBucket bucket
    ) {
        this.aggregationDate = aggregationDate;
        this.supplierPublicId = supplierPublicId;
        this.itemPublicId = itemPublicId;
        this.bucket = bucket;
        this.lotCount = 0L;
        this.totalQty = BigDecimal.ZERO;
    }

    public void add(Long lotCount, BigDecimal qty) {
        this.lotCount += lotCount;
        this.totalQty = this.totalQty.add(qty);
    }
}

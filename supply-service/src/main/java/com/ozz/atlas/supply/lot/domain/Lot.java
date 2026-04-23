package com.ozz.atlas.supply.lot.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lot")
public class Lot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lot_id")
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @Column(nullable = false, length = 50)
    private String lotNumber;

    @Column(nullable = false, length = 26)
    private String sourcePoItemPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_po_item_id")
    private SupplyPurchaseOrderItem sourcePoItem;

    public void setSourcePoItem(SupplyPurchaseOrderItem sourcePoItem) {
        this.sourcePoItem = sourcePoItem;
    }

    @Column(nullable = false, length = 26)
    private String supplierPublicId;

    @Column(nullable = false, length = 26)
    private String itemPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private SupplyItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private SupplySupplier supplier;

    public void setItem(SupplyItem item) {
        this.item = item;
    }

    public void setSupplier(SupplySupplier supplier) {
        this.supplier = supplier;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotStatus lotStatus;

    private LocalDateTime manufacturedAt;

    private LocalDateTime expiredAt;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal qty;

    @Column(nullable = false, length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QualityStatus qualityStatus;

    @Column(length = 26)
    private String currentNodePublicId;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = PublicIdGenerator.next();
        }
        if (this.lotStatus == null) {
            this.lotStatus = LotStatus.CREATED;
        }
        if (this.qualityStatus == null) {
            this.qualityStatus = QualityStatus.NORMAL;
        }
        if (this.qty == null) {
            this.qty = BigDecimal.ZERO;
        }
    }

    @Builder
    public Lot(String lotNumber, String sourcePoItemPublicId, String supplierPublicId, String itemPublicId, LocalDateTime manufacturedAt, LocalDateTime expiredAt, BigDecimal qty, String unit, String currentNodePublicId) {
        this.lotNumber = lotNumber;
        this.sourcePoItemPublicId = sourcePoItemPublicId;
        this.supplierPublicId = supplierPublicId;
        this.itemPublicId = itemPublicId;
        this.manufacturedAt = manufacturedAt;
        this.expiredAt = expiredAt;
        this.qty = qty;
        this.unit = unit;
        this.currentNodePublicId = currentNodePublicId;
        this.lotStatus = LotStatus.CREATED;
        this.qualityStatus = QualityStatus.NORMAL;
    }

    public void update(BigDecimal qty, LocalDateTime expiredAt, String currentNodePublicId) {
        if (qty != null) this.qty = qty;
        if (expiredAt != null) this.expiredAt = expiredAt;
        if (currentNodePublicId != null) this.currentNodePublicId = currentNodePublicId;
    }

    public void changeStatus(LotStatus status) {
        if (status != null) {
            this.lotStatus = status;
        }
    }

    public void changeQuality(QualityStatus quality) {
        if (quality != null) {
            this.qualityStatus = quality;
        }
    }
}
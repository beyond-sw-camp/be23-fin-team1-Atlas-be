package com.ozz.atlas.supply.lot.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
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

    @Column(nullable = false)
    private Long sourcePoItemId;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private Long itemId;

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

    private Long currentNodeId;

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
    public Lot(String lotNumber, Long sourcePoItemId, Long supplierId, Long itemId, LocalDateTime manufacturedAt, LocalDateTime expiredAt, BigDecimal qty, String unit, Long currentNodeId) {
        this.lotNumber = lotNumber;
        this.sourcePoItemId = sourcePoItemId;
        this.supplierId = supplierId;
        this.itemId = itemId;
        this.manufacturedAt = manufacturedAt;
        this.expiredAt = expiredAt;
        this.qty = qty;
        this.unit = unit;
        this.currentNodeId = currentNodeId;
        this.lotStatus = LotStatus.CREATED;
        this.qualityStatus = QualityStatus.NORMAL;
    }

    public void update(BigDecimal qty, LocalDateTime expiredAt, Long currentNodeId) {
        if (qty != null) this.qty = qty;
        if (expiredAt != null) this.expiredAt = expiredAt;
        if (currentNodeId != null) this.currentNodeId = currentNodeId;
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
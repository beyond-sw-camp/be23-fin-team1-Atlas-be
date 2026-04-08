package com.ozz.atlas.supply.supplier.capability.domain;

import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_item_capability_supplier_item", columnNames = {"supplier_id", "item_id"})
})
public class SupplySupplierItemCapability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierItemCapabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplySupplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyItem item;

    @Column(nullable = false)
    private Integer leadTimeDays;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monthlyCapacity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal availableQty;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal moq;

    @Convert(converter = SupplierItemQualityGradeConverter.class)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private SupplierItemQualityGrade qualityGrade = SupplierItemQualityGrade.AA_PLUS;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPriceHint;

    private LocalDate validFrom;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static SupplySupplierItemCapability create(
            SupplySupplier supplier,
            SupplyItem item,
            Integer leadTimeDays,
            BigDecimal monthlyCapacity,
            BigDecimal availableQty,
            BigDecimal moq,
            SupplierItemQualityGrade qualityGrade,
            BigDecimal unitPriceHint,
            LocalDate validFrom
    ) {
        return SupplySupplierItemCapability.builder()
                .supplier(supplier)
                .item(item)
                .leadTimeDays(leadTimeDays)
                .monthlyCapacity(monthlyCapacity)
                .availableQty(availableQty)
                .moq(moq)
                .qualityGrade(qualityGrade != null ? qualityGrade : SupplierItemQualityGrade.AA_PLUS)
                .unitPriceHint(unitPriceHint)
                .validFrom(validFrom)
                .build();
    }

    public void update(
            Integer leadTimeDays,
            BigDecimal monthlyCapacity,
            BigDecimal availableQty,
            BigDecimal moq,
            SupplierItemQualityGrade qualityGrade,
            BigDecimal unitPriceHint,
            LocalDate validFrom
    ) {
        if (leadTimeDays != null) {
            this.leadTimeDays = leadTimeDays;
        }
        if (monthlyCapacity != null) {
            this.monthlyCapacity = monthlyCapacity;
        }
        if (availableQty != null) {
            this.availableQty = availableQty;
        }
        if (moq != null) {
            this.moq = moq;
        }
        if (qualityGrade != null) {
            this.qualityGrade = qualityGrade;
        }
        if (unitPriceHint != null) {
            this.unitPriceHint = unitPriceHint;
        }
        if (validFrom != null) {
            this.validFrom = validFrom;
        }
    }
}

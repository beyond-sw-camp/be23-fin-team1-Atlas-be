package com.ozz.atlas.supply.supplier.relation.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Entity
@Table(name = "supply_supplier_relation")
public class SupplySupplierRelation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_relation_id")
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_supplier_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private SupplySupplier parentSupplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_supplier_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private SupplySupplier childSupplier;

    @Column(name = "priority_rank", nullable = false)
    @Builder.Default
    private Integer priorityRank = 1;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SupplierRelationStatus relationStatus = SupplierRelationStatus.REQUESTED;

    public static SupplySupplierRelation create(
            SupplySupplier parentSupplier,
            SupplySupplier childSupplier,
            Integer priorityRank,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        return SupplySupplierRelation.builder()
                .parentSupplier(parentSupplier)
                .childSupplier(childSupplier)
                .priorityRank(priorityRank != null ? priorityRank : 1)
                .relationStatus(SupplierRelationStatus.REQUESTED)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .build();
    }

    public void update(
            Integer priorityRank,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        if (priorityRank != null) this.priorityRank = priorityRank;
        if (effectiveFrom != null) this.effectiveFrom = effectiveFrom;
        if (effectiveTo != null) this.effectiveTo = effectiveTo;
    }

    public void markRequested() {
        this.relationStatus = SupplierRelationStatus.REQUESTED;
        if (this.effectiveFrom == null) {
            this.effectiveFrom = LocalDate.now();
        }
        this.effectiveTo = null;
    }

    public void markActive() {
        this.relationStatus = SupplierRelationStatus.ACTIVE;
        if (this.effectiveFrom == null) {
            this.effectiveFrom = LocalDate.now();
        }
        this.effectiveTo = null;
    }

    public void markPaused() {
        this.relationStatus = SupplierRelationStatus.PAUSED;
    }

    public void markEnded(LocalDate endedAt) {
        this.relationStatus = SupplierRelationStatus.ENDED;
        this.effectiveTo = endedAt != null ? endedAt : LocalDate.now();
    }

}

package com.ozz.atlas.supply.supplier.relation.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    private SupplierRelationType relationType;

    @Column(name = "priority_rank", nullable = false)
    @Builder.Default
    private Integer priorityRank = 1;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    public static SupplySupplierRelation create(
            SupplySupplier parentSupplier,
            SupplySupplier childSupplier,
            SupplierRelationType relationType,
            Integer priorityRank,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        return SupplySupplierRelation.builder()
                .parentSupplier(parentSupplier)
                .childSupplier(childSupplier)
                .relationType(relationType)
                .priorityRank(priorityRank != null ? priorityRank : 1)
                .status(Status.ACTIVE)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .build();
    }

    public void update(
            SupplierRelationType relationType,
            Integer priorityRank,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        if (relationType != null) this.relationType = relationType;
        if (priorityRank != null) this.priorityRank = priorityRank;
        if (effectiveFrom != null) this.effectiveFrom = effectiveFrom;
        if (effectiveTo != null) this.effectiveTo = effectiveTo;
    }

    public void deactivate(LocalDate endedAt) {
        this.status = Status.DELETE;
        if (this.effectiveTo == null) {
            this.effectiveTo = endedAt;
        }
    }
}

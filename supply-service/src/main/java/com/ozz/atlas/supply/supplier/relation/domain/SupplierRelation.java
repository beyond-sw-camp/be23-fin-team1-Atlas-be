package com.ozz.atlas.supply.supplier.relation.domain;

import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class SupplierRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_supplier_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private SupplySupplier parentSupplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_supplier_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private SupplySupplier childSupplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierRelationType relationType;

    @Column(nullable = false)
    private Integer priorityRank;

    @Column(nullable = false)
    private Boolean activeYn;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.priorityRank == null) {
            this.priorityRank = 1;
        }
        if (this.activeYn == null) {
            this.activeYn = Boolean.TRUE;
        }
    }
}

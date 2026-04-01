package com.ozz.atlas.supply.supplier.esg.domain;

import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class SupplyEsgAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplySupplier supplier;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal environmentScore;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal socialScore;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal governanceScore;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore;
    private Long grade; //이거 Enum???
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;
    private String evaluatorName;
    @Column(columnDefinition = "TEXT")
    private String note;
}

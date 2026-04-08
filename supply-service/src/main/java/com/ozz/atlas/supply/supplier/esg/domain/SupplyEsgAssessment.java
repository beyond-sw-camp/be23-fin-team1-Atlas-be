package com.ozz.atlas.supply.supplier.esg.domain;

import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Entity
@Table(name = "supply_esg_assessment")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EsgGrade grade;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;

    @Column(length = 50)
    private String evaluatorName;

    @Column(columnDefinition = "TEXT")
    private String note;

    public static SupplyEsgAssessment create(
            SupplySupplier supplier,
            BigDecimal environmentScore,
            BigDecimal socialScore,
            BigDecimal governanceScore,
            String evaluatorName,
            String note
    ) {
        BigDecimal totalScore = calculateTotalScore(environmentScore, socialScore, governanceScore);

        return SupplyEsgAssessment.builder()
                .supplier(supplier)
                .environmentScore(environmentScore)
                .socialScore(socialScore)
                .governanceScore(governanceScore)
                .totalScore(totalScore)
                .grade(EsgGrade.fromScore(totalScore))
                .evaluatorName(evaluatorName)
                .note(note)
                .build();
    }

    public void update(
            BigDecimal environmentScore,
            BigDecimal socialScore,
            BigDecimal governanceScore,
            String evaluatorName,
            String note
    ) {
        if (environmentScore != null) {
            this.environmentScore = environmentScore;
        }
        if (socialScore != null) {
            this.socialScore = socialScore;
        }
        if (governanceScore != null) {
            this.governanceScore = governanceScore;
        }
        if (evaluatorName != null) {
            this.evaluatorName = evaluatorName;
        }
        if (note != null) {
            this.note = note;
        }

        this.totalScore = calculateTotalScore(
                this.environmentScore,
                this.socialScore,
                this.governanceScore
        );
        this.grade = EsgGrade.fromScore(this.totalScore);
    }


    private static BigDecimal calculateTotalScore(
            BigDecimal environmentScore,
            BigDecimal socialScore,
            BigDecimal governanceScore
    ) {
        return environmentScore
                .add(socialScore)
                .add(governanceScore)
                .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
    }
}

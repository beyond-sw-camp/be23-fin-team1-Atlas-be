package com.ozz.atlas.supply.lot.linemapping.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LotLineMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lotLineMappingId;

    @Column(nullable = false, length = 26)
    private String lotPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private ProductionLine productionLine;

    @Column(precision = 18, scale = 2)
    private BigDecimal processedQty;

    private LocalDateTime processStartedAt;

    private LocalDateTime processEndedAt;

    @Column(columnDefinition = "TEXT")
    private String mappingNote;

    public static LotLineMapping create(
            String lotPublicId,
            ProductionLine productionLine,
            BigDecimal processedQty,
            String mappingNote
    ) {
        return LotLineMapping.builder()
                .lotPublicId(lotPublicId)
                .productionLine(productionLine)
                .processedQty(processedQty)
                .mappingNote(mappingNote)
                .build();
    }

    public void update(BigDecimal processedQty, String mappingNote) {
        if (processedQty != null) {
            this.processedQty = processedQty;
        }
        if (mappingNote != null) {
            this.mappingNote = mappingNote;
        }
    }

    public void start() {
        if (this.processStartedAt == null) {
            this.processStartedAt = LocalDateTime.now();
        }
    }

    public void complete() {
        if (this.processStartedAt == null) {
            throw new IllegalStateException("작업 시작 전에는 완료 처리할 수 없습니다.");
        }
        if (this.processEndedAt == null) {
            this.processEndedAt = LocalDateTime.now();
        }
    }
}

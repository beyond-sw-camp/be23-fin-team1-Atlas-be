package com.ozz.atlas.supply.productionline.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class ProductionLine extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productionLineId;

    @Column(nullable = false, length = 26)
    private String logisticsNodePublicId;

    @Column(nullable = false, length = 50)
    private String lineCode;

    @Column(nullable = false, length = 100)
    private String lineName;

    @Column(length = 30)
    private String lineType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(precision = 18, scale = 2)
    private BigDecimal dailyCapacity;
}

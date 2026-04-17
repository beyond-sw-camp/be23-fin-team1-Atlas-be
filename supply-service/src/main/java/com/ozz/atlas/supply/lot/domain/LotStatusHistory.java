package com.ozz.atlas.supply.lot.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lot_status_history")
public class LotStatusHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lot_status_history_id")
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LotStatus preLotStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotStatus lotStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QualityStatus preQualityStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QualityStatus qualityStatus;

    @Column(length = 26)
    private String preNodePublicId;

    @Column(length = 26)
    private String currentNodePublicId;

    @Column(length = 200)
    private String reason;

    @Builder
    public LotStatusHistory(Lot lot, LotStatus preLotStatus, LotStatus lotStatus, QualityStatus preQualityStatus, QualityStatus qualityStatus, String preNodePublicId, String currentNodePublicId, String reason) {
        this.publicId = PublicIdGenerator.next();
        this.lot = lot;
        this.preLotStatus = preLotStatus;
        this.lotStatus = lotStatus;
        this.preQualityStatus = preQualityStatus;
        this.qualityStatus = qualityStatus;
        this.preNodePublicId = preNodePublicId;
        this.currentNodePublicId = currentNodePublicId;
        this.reason = reason;
    }
}

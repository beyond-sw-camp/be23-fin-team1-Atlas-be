package com.ozz.atlas.supply.returns.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "return_item")
public class ReturnItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnRequest returnRequest;

    @Column(nullable = false, length = 26)
    private String itemPublicId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal returnQty;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(length = 255)
    private String detailReason;

    @Column(nullable = false, length = 20)
    private String itemStatus;

    @Column(length = 20)
    private String qcStatus; // PENDING, PASS, FAIL

    @Column(length = 20)
    private String qcGrade; // A, B, DEFECTIVE

    @Column(length = 50)
    private String disposalReason; // EXPIRED, DAMAGED, CONTAMINATED

    @Column(length = 26)
    private String disposalProofAttachmentPublicId;

    @Column(columnDefinition = "TEXT")
    private String attachmentPublicIds;

    @Builder
    public ReturnItem(String itemPublicId, BigDecimal returnQty, String unit, String detailReason, String attachmentPublicIds) {
        this.itemPublicId = itemPublicId;
        this.returnQty = returnQty;
        this.unit = unit;
        this.detailReason = detailReason;
        this.attachmentPublicIds = attachmentPublicIds;
        this.itemStatus = "REQUESTED";
        this.qcStatus = "PENDING";
    }

    public void setReturnRequest(ReturnRequest returnRequest) {
        this.returnRequest = returnRequest;
    }

    public void update(BigDecimal returnQty, String detailReason, String attachmentPublicIds) {
        if (this.returnRequest.getReturnStatus() != ReturnStatus.REQUESTED) {
            throw new IllegalStateException("반품 요청 상태에서만 수정할 수 있습니다.");
        }
        if (returnQty != null) this.returnQty = returnQty;
        if (detailReason != null) this.detailReason = detailReason;
        if (attachmentPublicIds != null) this.attachmentPublicIds = attachmentPublicIds;
    }

    public void inspect(String qcStatus, String qcGrade, String description) {
        this.qcStatus = qcStatus;
        this.qcGrade = qcGrade;
        this.detailReason = description;
        if ("PASS".equals(qcStatus)) {
            this.itemStatus = "INSPECTED_PASS";
        } else {
            this.itemStatus = "INSPECTED_FAIL";
        }
    }

    public void setDisposalInfo(String reason, String proofAttachmentPublicId) {
        this.disposalReason = reason;
        this.disposalProofAttachmentPublicId = proofAttachmentPublicId;
    }
}
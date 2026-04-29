package com.ozz.atlas.supply.returns.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "return_request")
public class ReturnRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @Column(nullable = false, length = 50)
    private String returnNumber;

    @Column(length = 26)
    private String sourceShipmentPublicId;

    @Column(length = 26)
    private String returnShipmentPublicId;

    @Column(nullable = false, length = 26)
    private String requestOrganizationPublicId;

    @Column(nullable = false, length = 26)
    private String targetOrganizationPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnType returnType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResolutionType resolutionType = ResolutionType.RETURN;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String returnReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus returnStatus;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime completedAt;

    @Column(length = 26)
    private String createdByUserPublicId;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnItem> items = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String attachmentPublicIds;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = PublicIdGenerator.next();
        }
        if (this.returnStatus == null) {
            this.returnStatus = ReturnStatus.REQUESTED;
        }
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
        if (this.resolutionType == null) {
            this.resolutionType = ResolutionType.RETURN;
        }
    }

    @Builder
    public ReturnRequest(String returnNumber, String sourceShipmentPublicId, String requestOrganizationPublicId, String targetOrganizationPublicId, ReturnType returnType, ResolutionType resolutionType, String returnReason, String createdByUserPublicId, String attachmentPublicIds) {
        this.returnNumber = returnNumber;
        this.sourceShipmentPublicId = sourceShipmentPublicId;
        this.requestOrganizationPublicId = requestOrganizationPublicId;
        this.targetOrganizationPublicId = targetOrganizationPublicId;
        this.returnType = returnType;
        this.resolutionType = resolutionType != null ? resolutionType : ResolutionType.RETURN;
        this.returnReason = returnReason;
        this.createdByUserPublicId = createdByUserPublicId;
        this.attachmentPublicIds = attachmentPublicIds;
        this.returnStatus = ReturnStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    public void addItem(ReturnItem item) {
        this.items.add(item);
        item.setReturnRequest(this);
    }

    public void update(ReturnType returnType, ResolutionType resolutionType, String returnReason, String attachmentPublicIds) {
        if (this.returnStatus != ReturnStatus.REQUESTED) {
            throw new IllegalStateException("반품 요청 상태에서만 수정할 수 있습니다.");
        }
        if (returnType != null) this.returnType = returnType;
        if (resolutionType != null) this.resolutionType = resolutionType;
        if (returnReason != null) this.returnReason = returnReason;
        if (attachmentPublicIds != null) this.attachmentPublicIds = attachmentPublicIds;
    }

    public void changeStatus(ReturnStatus newStatus) {
        // Prevent reverting from final states
        if (this.returnStatus == ReturnStatus.REJECTED || this.returnStatus == ReturnStatus.COMPLETED) {
            throw new IllegalStateException("반려되거나 완료된 반품은 상태를 변경할 수 없습니다.");
        }

        this.returnStatus = newStatus;
        if (newStatus == ReturnStatus.APPROVED) {
            this.approvedAt = LocalDateTime.now();
        } else if (newStatus == ReturnStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }
    public void assignReturnShipmentPublicId(String returnShipmentPublicId) {
        if (this.returnShipmentPublicId != null && !this.returnShipmentPublicId.isBlank()) {
            throw new IllegalStateException("이미 반품출하가 생성된 반품 요청입니다.");
        }
        this.returnShipmentPublicId = returnShipmentPublicId;
    }

}
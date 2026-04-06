package com.ozz.atlas.supply.onboarding.domain;

import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class OnboardingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private SupplySupplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OnboardingRequestType requestType = OnboardingRequestType.NEW_REGISTRATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OnboardingRequestStatus requestStatus = OnboardingRequestStatus.REQUESTED;

    @Column(nullable = false, updatable = false, length = 26)
    private String requestedByUserPublicId;

    @Column(length = 26)
    private String reviewedByUserPublicId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    @Column(columnDefinition = "TEXT")
    private String note;

    public static OnboardingRequest create(SupplySupplier supplier, String requestedByUserPublicId) {
        return OnboardingRequest.builder()
                .supplier(supplier)
                .requestType(OnboardingRequestType.NEW_REGISTRATION)
                .requestStatus(OnboardingRequestStatus.REQUESTED)
                .requestedByUserPublicId(requestedByUserPublicId)
                .build();
    }

    public void approve(String reviewedByUserPublicId) {
        this.requestStatus = OnboardingRequestStatus.APPROVED;
        this.reviewedByUserPublicId = reviewedByUserPublicId;
        this.reviewedAt = LocalDateTime.now();
        this.rejectReason = null;
    }

    public void reject(String reviewedByUserPublicId, String rejectReason) {
        this.requestStatus = OnboardingRequestStatus.REJECTED;
        this.reviewedByUserPublicId = reviewedByUserPublicId;
        this.reviewedAt = LocalDateTime.now();
        this.rejectReason = rejectReason;
    }
}

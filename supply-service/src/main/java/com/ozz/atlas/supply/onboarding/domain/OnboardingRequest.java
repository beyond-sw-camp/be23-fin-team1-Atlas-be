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
    private OnboardingRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingRequestStatus requestStatus;

    @Column(length = 26, unique = true)
    private String requestedByUserPublicId;

    @Column(length = 26, unique = true)
    private String reviewedByUserPublicId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    public void prePersist() {
        if (this.requestStatus == null) {
            this.requestStatus = OnboardingRequestStatus.REQUESTED;
        }
    }
}

package com.ozz.atlas.supply.shipment.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class DeliveryException extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shipmentId;

    private Long shipmentCheckpointId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryExceptionType exceptionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryExceptionSeverity severity;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean resolved = false;

    private LocalDateTime resolvedAt;

    @Column(length = 255)
    private String note;

    @PrePersist
    public void prePersist() {
        if (this.detectedAt == null) {
            this.detectedAt = LocalDateTime.now();
        }
    }

//    이상 해소
    public void markResolved(LocalDateTime resolvedAt) {
        this.resolved = true;
        this.resolvedAt = resolvedAt != null ? resolvedAt : LocalDateTime.now();
    }
}

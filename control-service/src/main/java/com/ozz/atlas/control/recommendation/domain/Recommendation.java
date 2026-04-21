package com.ozz.atlas.control.recommendation.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(name = "source_event_id", nullable = false, length = 26)
    private String sourceEventId;

    @Column(name = "source_event_type", nullable = false, length = 120)
    private String sourceEventType;

    @Column(name = "shipment_public_id", nullable = false, length = 26)
    private String shipmentPublicId;

    @Column(name = "risk_type", nullable = false, length = 80)
    private String riskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_status", nullable = false, length = 30)
    private RecommendationStatus recommendationStatus;

    @Column(name = "provider", length = 80)
    private String provider;

    @Column(name = "model", length = 120)
    private String model;

    @Column(name = "model_version", length = 120)
    private String modelVersion;

    @Column(name = "summary", nullable = false, length = 1000)
    private String summary;

    @Column(name = "failure_reason", length = 2000)
    private String failureReason;

    @Column(name = "actor_user_public_id", length = 26)
    private String actorUserPublicId;

    @Column(name = "organization_public_id", length = 26)
    private String organizationPublicId;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecommendationItem> items = new ArrayList<>();

    public static Recommendation generated(
            String publicId,
            String sourceEventId,
            String sourceEventType,
            String shipmentPublicId,
            String riskType,
            String provider,
            String model,
            String modelVersion,
            String summary,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        return Recommendation.builder()
                .publicId(publicId)
                .sourceEventId(sourceEventId)
                .sourceEventType(sourceEventType)
                .shipmentPublicId(shipmentPublicId)
                .riskType(riskType)
                .recommendationStatus(RecommendationStatus.GENERATED)
                .provider(provider)
                .model(model)
                .modelVersion(modelVersion)
                .summary(summary)
                .actorUserPublicId(actorUserPublicId)
                .organizationPublicId(organizationPublicId)
                .build();
    }

    public static Recommendation failed(
            String publicId,
            String sourceEventId,
            String sourceEventType,
            String shipmentPublicId,
            String riskType,
            String summary,
            String failureReason,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        return Recommendation.builder()
                .publicId(publicId)
                .sourceEventId(sourceEventId)
                .sourceEventType(sourceEventType)
                .shipmentPublicId(shipmentPublicId)
                .riskType(riskType)
                .recommendationStatus(RecommendationStatus.FAILED)
                .summary(summary)
                .failureReason(failureReason)
                .actorUserPublicId(actorUserPublicId)
                .organizationPublicId(organizationPublicId)
                .build();
    }

    public void applyGenerated(
            String sourceEventId,
            String sourceEventType,
            String shipmentPublicId,
            String riskType,
            String provider,
            String model,
            String modelVersion,
            String summary,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        this.sourceEventId = sourceEventId;
        this.sourceEventType = sourceEventType;
        this.shipmentPublicId = shipmentPublicId;
        this.riskType = riskType;
        this.recommendationStatus = RecommendationStatus.GENERATED;
        this.provider = provider;
        this.model = model;
        this.modelVersion = modelVersion;
        this.summary = summary;
        this.failureReason = null;
        this.actorUserPublicId = actorUserPublicId;
        this.organizationPublicId = organizationPublicId;
    }

    public void applyFailed(
            String sourceEventId,
            String sourceEventType,
            String shipmentPublicId,
            String riskType,
            String summary,
            String failureReason,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        this.sourceEventId = sourceEventId;
        this.sourceEventType = sourceEventType;
        this.shipmentPublicId = shipmentPublicId;
        this.riskType = riskType;
        this.recommendationStatus = RecommendationStatus.FAILED;
        this.provider = null;
        this.model = null;
        this.modelVersion = null;
        this.summary = summary;
        this.failureReason = failureReason;
        this.actorUserPublicId = actorUserPublicId;
        this.organizationPublicId = organizationPublicId;
        this.items.clear();
    }

    public void replaceItems(List<RecommendationItem> recommendationItems) {
        this.items.clear();

        for (RecommendationItem item : recommendationItems) {
            item.attach(this);
            this.items.add(item);
        }
    }

    public void markAccepted() {
        this.recommendationStatus = RecommendationStatus.ACCEPTED;
    }

    public void markRejected() {
        this.recommendationStatus = RecommendationStatus.REJECTED;
    }
}

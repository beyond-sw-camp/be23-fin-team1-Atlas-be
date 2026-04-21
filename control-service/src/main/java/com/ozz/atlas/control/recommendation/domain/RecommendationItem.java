package com.ozz.atlas.control.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation_item")
@Getter
@NoArgsConstructor
public class RecommendationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "action", nullable = false, length = 1000)
    private String action;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Builder
    private RecommendationItem(
            Recommendation recommendation,
            int sequenceNo,
            String title,
            String reason,
            String action,
            int priority,
            double confidence
    ) {
        this.recommendation = recommendation;
        this.sequenceNo = sequenceNo;
        this.title = title;
        this.reason = reason;
        this.action = action;
        this.priority = priority;
        this.confidence = confidence;
    }

    public static RecommendationItem create(
            int sequenceNo,
            String title,
            String reason,
            String action,
            int priority,
            double confidence
    ) {
        return RecommendationItem.builder()
                .sequenceNo(sequenceNo)
                .title(title)
                .reason(reason)
                .action(action)
                .priority(priority)
                .confidence(confidence)
                .build();
    }

    void attach(Recommendation recommendation) {
        this.recommendation = recommendation;
    }
}

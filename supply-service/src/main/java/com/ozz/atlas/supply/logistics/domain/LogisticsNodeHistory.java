package com.ozz.atlas.supply.logistics.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "logistics_node_history")
public class LogisticsNodeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logistics_node_history_id")
    private Long id;

    @Column(nullable = false)
    private Long logisticsNodeId;

    @Column(nullable = false, length = 26)
    private String logisticsNodePublicId;

    @Column(nullable = false, length = 26)
    private String organizationPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LogisticsNodeHistoryChangeType changeType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private LogisticsNodeCapacityStatus beforeCapacityStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private LogisticsNodeCapacityStatus afterCapacityStatus;

    private Boolean beforeActive;

    private Boolean afterActive;

    @Column(length = 100)
    private String nodeName;

    @Column(length = 512)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(length = 26)
    private String recordedBy;

    @PrePersist
    public void prePersist() {
        if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }
    }

    @Builder
    public LogisticsNodeHistory(
            Long logisticsNodeId,
            String logisticsNodePublicId,
            String organizationPublicId,
            LogisticsNodeHistoryChangeType changeType,
            LogisticsNodeCapacityStatus beforeCapacityStatus,
            LogisticsNodeCapacityStatus afterCapacityStatus,
            Boolean beforeActive,
            Boolean afterActive,
            String nodeName,
            String address,
            String memo,
            String recordedBy
    ) {
        this.logisticsNodeId = logisticsNodeId;
        this.logisticsNodePublicId = logisticsNodePublicId;
        this.organizationPublicId = organizationPublicId;
        this.changeType = changeType;
        this.beforeCapacityStatus = beforeCapacityStatus;
        this.afterCapacityStatus = afterCapacityStatus;
        this.beforeActive = beforeActive;
        this.afterActive = afterActive;
        this.nodeName = nodeName;
        this.address = address;
        this.memo = memo;
        this.recordedBy = recordedBy;
    }
}

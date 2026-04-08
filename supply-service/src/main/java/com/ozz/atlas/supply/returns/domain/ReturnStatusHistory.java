package com.ozz.atlas.supply.returns.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "return_status_history")
public class ReturnStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_status_history_id")
    private Long id;

    @Column(nullable = false)
    private Long returnRequestId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReturnStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus afterStatus;

    @Column(columnDefinition = "TEXT")
    private String reason;

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
    public ReturnStatusHistory(Long returnRequestId, ReturnStatus beforeStatus, ReturnStatus afterStatus, String reason, String recordedBy) {
        this.returnRequestId = returnRequestId;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.reason = reason;
        this.recordedBy = recordedBy;
    }
}
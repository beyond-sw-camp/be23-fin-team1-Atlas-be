package com.ozz.atlas.supply.item.domain;

import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "supply_item_history")
public class SupplyItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, length = 26)
    private String itemPublicId;

    @Column(nullable = false, length = 50)
    private String itemCode;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SupplyItemHistoryActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SupplyType beforeSupplyType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SupplyType afterSupplyType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status afterStatus;

    @Column(length = 26)
    private String beforePrimaryMediaFilePublicId;

    @Column(length = 26)
    private String afterPrimaryMediaFilePublicId;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(length = 26)
    private String recordedBy;

    @PrePersist
    void prePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}

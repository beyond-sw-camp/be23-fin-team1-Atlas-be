package com.ozz.atlas.supply.item.domain;

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
public class SupplyItemCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyItemCategory parentCategory;

    @Column(nullable = false, length = 100)
    private String categoryName;
    @Column(nullable = false)
    private Integer categoryLevel;
    @Column(nullable = false)
    private Integer sortOrder;
    @Column(nullable = false)
    private Integer activeYn;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.categoryLevel == null) {
            this.categoryLevel = 1;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 1;
        }
        if (this.activeYn == null) {
            this.activeYn = 1;
        }
    }
}

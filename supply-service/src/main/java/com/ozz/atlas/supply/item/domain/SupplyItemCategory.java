package com.ozz.atlas.supply.item.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class SupplyItemCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyItemCategory parentCategory;

    @Column(nullable = false, length = 100)
    private String categoryName;

    @Column(nullable = false)
    private Integer categoryLevel;

    @Column(nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @PrePersist
    public void prePersist() {
        if (this.categoryLevel == null) {
            this.categoryLevel = 1;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 1;
        }
    }

    public static SupplyItemCategory create(
            SupplyItemCategory parentCategory,
            String categoryName,
            Integer categoryLevel,
            Integer sortOrder
    ) {
        return SupplyItemCategory.builder()
                .parentCategory(parentCategory)
                .categoryName(categoryName)
                .categoryLevel(categoryLevel)
                .sortOrder(sortOrder)
                .status(Status.ACTIVE)
                .build();
    }

    public void update(
            SupplyItemCategory parentCategory,
            String categoryName,
            Integer categoryLevel,
            Integer sortOrder
    ) {
        this.parentCategory = parentCategory;
        this.categoryName = categoryName;
        this.categoryLevel = categoryLevel;
        this.sortOrder = sortOrder;
    }

    public void changeActiveYn(Status status) {
        this.status = status;
    }
}

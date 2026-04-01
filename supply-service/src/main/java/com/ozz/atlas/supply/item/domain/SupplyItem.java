package com.ozz.atlas.supply.item.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class SupplyItem extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyItemCategory itemCategory;

    @Column(nullable = false, length = 50)
    private String itemCode;
    @Column(nullable = false, length = 100)
    private String itemName;
    @Column(nullable = false, length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSpec spec;

    @Column(nullable = false)
    private Integer shelfLifeDays;
    @Column(nullable = false)
    private Integer activeYn;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null || this.publicId.isBlank()) {
            this.publicId = PublicIdGenerator.next();
        }
        if (this.activeYn == null) {
            this.activeYn = 1;
        }
    }

    public static SupplyItem create(
            SupplyItemCategory itemCategory,
            String itemCode,
            String itemName,
            String unit,
            ItemSpec spec,
            Integer shelfLifeDays
    ) {
        return SupplyItem.builder()
                .itemCategory(itemCategory)
                .itemCode(itemCode)
                .itemName(itemName)
                .unit(unit)
                .spec(spec)
                .shelfLifeDays(shelfLifeDays)
                .activeYn(1)
                .build();
    }

    public void update(
            SupplyItemCategory itemCategory,
            String itemCode,
            String itemName,
            String unit,
            ItemSpec spec,
            Integer shelfLifeDays,
            Integer activeYn
    ) {
        this.itemCategory = itemCategory;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.unit = unit;
        this.spec = spec;
        this.shelfLifeDays = shelfLifeDays;
        this.activeYn = activeYn;
    }

    public void changeActiveYn(Integer activeYn) {
        this.activeYn = activeYn;
    }
}

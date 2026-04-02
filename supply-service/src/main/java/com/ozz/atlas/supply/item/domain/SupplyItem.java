package com.ozz.atlas.supply.item.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
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
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.DEACTIVE;

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
                .status(Status.DEACTIVE)
                .build();
    }

    public void update(
            SupplyItemCategory itemCategory,
            String itemCode,
            String itemName,
            String unit,
            ItemSpec spec,
            Integer shelfLifeDays
    ) {
        this.itemCategory = itemCategory;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.unit = unit;
        this.spec = spec;
        this.shelfLifeDays = shelfLifeDays;
        this.status = Status.ACTIVE;
    }

    public void changeActiveYn(Status status) {
        this.status = status;
    }
}

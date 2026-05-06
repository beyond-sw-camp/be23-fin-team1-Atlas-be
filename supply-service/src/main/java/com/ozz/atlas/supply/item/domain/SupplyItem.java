package com.ozz.atlas.supply.item.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
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
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplySupplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyItemCategory itemCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SupplyType supplyType = SupplyType.STOCK_BASED;

    @Column(nullable = false, length = 50)
    private String itemCode;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemUnit unit;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 100)
    private String spec;

    @Column(nullable = false)
    private Integer shelfLifeDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(length = 26)
    private String primaryMediaFilePublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_logistics_node_id")
    private LogisticsNode originLogisticsNode;

    public static SupplyItem create(
            SupplySupplier supplier,
            SupplyItemCategory itemCategory,
            LogisticsNode originLogisticsNode,
            String itemCode,
            String itemName,
            ItemUnit unit,
            BigDecimal unitPrice,
            String spec,
            Integer shelfLifeDays,
            SupplyType supplyType
    ) {
        return SupplyItem.builder()
                .supplier(supplier)
                .itemCategory(itemCategory)
                .originLogisticsNode(originLogisticsNode)
                .itemCode(itemCode)
                .itemName(itemName)
                .unit(unit)
                .unitPrice(unitPrice)
                .spec(spec)
                .shelfLifeDays(shelfLifeDays)
                .supplyType(supplyType != null ? supplyType : SupplyType.STOCK_BASED)
                .status(Status.ACTIVE)
                .build();
    }



    public void update(
            SupplyItemCategory itemCategory,
            LogisticsNode originLogisticsNode,
            String itemName,
            ItemUnit unit,
            BigDecimal unitPrice,
            String spec,
            Integer shelfLifeDays,
            SupplyType supplyType
    ) {
        this.itemCategory = itemCategory;
        this.originLogisticsNode = originLogisticsNode;
        this.itemName = itemName;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.spec = spec;
        this.shelfLifeDays = shelfLifeDays;
        this.supplyType = supplyType != null ? supplyType : SupplyType.STOCK_BASED;
        this.status = Status.ACTIVE;
    }


    public void changeActiveYn(Status status) {
        this.status = status;
    }

    public void changePrimaryMedia(String filePublicId) {
        this.primaryMediaFilePublicId = filePublicId;
    }
}

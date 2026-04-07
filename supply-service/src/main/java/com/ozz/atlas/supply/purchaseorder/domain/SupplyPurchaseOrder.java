package com.ozz.atlas.supply.purchaseorder.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Entity
public class SupplyPurchaseOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, length = 50)
    private String poNumber;

    @Column(nullable = false, length = 26)
    private String buyerOrganizationPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplySupplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PoStatus poStatus = PoStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PriorityCode priorityCode = PriorityCode.NORMAL;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CurrencyCode currencyCode = CurrencyCode.KRW;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(length = 26)
    private String createdByUserPublicId;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("poItemId ASC")
    @Builder.Default
    private List<SupplyPurchaseOrderItem> purchaseOrderItems = new ArrayList<>();
}

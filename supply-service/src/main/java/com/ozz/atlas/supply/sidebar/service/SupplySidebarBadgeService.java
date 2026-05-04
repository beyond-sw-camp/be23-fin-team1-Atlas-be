package com.ozz.atlas.supply.sidebar.service;

import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.repository.SupplyItemInventoryRepository;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.repository.SettlementRepository;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.sidebar.domain.SupplyDetailViewState;
import com.ozz.atlas.supply.sidebar.dto.SupplySidebarBadgesResponse;
import com.ozz.atlas.supply.sidebar.repository.SupplyDetailViewStateRepository;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.repository.SupplierCertificateRepository;
import com.ozz.atlas.supply.supplier.relation.domain.SupplierRelationStatus;
import com.ozz.atlas.supply.supplier.relation.repository.SupplierRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupplySidebarBadgeService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final SupplierRelationRepository supplierRelationRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final SupplyItemInventoryRepository inventoryRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final ShipmentRepository shipmentRepository;
    private final SettlementRepository settlementRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final SupplierCertificateRepository supplierCertificateRepository;
    private final SupplyDetailViewStateRepository detailViewStateRepository;

    @Transactional(readOnly = true)
    public SupplySidebarBadgesResponse getBadges(String userPublicId, String organizationPublicId, String organizationType) {
        if (organizationPublicId == null || organizationPublicId.isBlank() || "ADMIN".equalsIgnoreCase(organizationType)) {
            return emptyResponse();
        }

        List<PoStatus> purchaseOrderAttentionStatuses = List.of(PoStatus.CREATED, PoStatus.PARTIALLY_CONFIRMED);
        List<SubPoStatus> subPurchaseOrderAttentionStatuses = List.of(SubPoStatus.CREATED, SubPoStatus.PARTIALLY_CONFIRMED);
        Set<String> orderIds = new LinkedHashSet<>();
        orderIds.addAll(purchaseOrderRepository.findRelatedPurchaseOrderPublicIdsByStatuses(
                organizationPublicId,
                purchaseOrderAttentionStatuses
        ));
        orderIds.addAll(subPurchaseOrderRepository.findReadablePublicIdsByOrganizationPublicIdAndStatuses(
                organizationPublicId,
                subPurchaseOrderAttentionStatuses
        ));

        List<String> supplierIds = supplierRelationRepository.findAttentionRelationPublicIdsByOrganizationPublicId(
                organizationPublicId,
                List.of(SupplierRelationStatus.REQUESTED, SupplierRelationStatus.PAUSED)
        );

        List<String> itemIds = supplyItemRepository.findAllBySupplier_OrganizationPublicIdAndStatusIn(
                organizationPublicId,
                List.of(Status.DEACTIVE)
        ).stream().map(item -> item.getPublicId()).toList();

        List<String> inventoryIds = inventoryRepository.findAttentionInventoryPublicIdsByOrganizationPublicId(
                organizationPublicId,
                List.of(InventoryStatus.EXHAUSTED, InventoryStatus.EXPIRED)
        );

        Set<String> logisticsNodeIds = new LinkedHashSet<>();
        logisticsNodeIds.addAll(logisticsNodeRepository.findByOrganizationPublicIdAndActiveFalse(organizationPublicId)
                .stream().map(node -> node.getPublicId()).toList());
        logisticsNodeIds.addAll(logisticsNodeRepository.findByOrganizationPublicIdAndCapacityStatus(
                organizationPublicId,
                LogisticsNodeCapacityStatus.FULL
        ).stream().map(node -> node.getPublicId()).toList());

        List<Long> nodeIds = logisticsNodeRepository.findByOrganizationPublicId(organizationPublicId)
                .stream()
                .map(node -> node.getId())
                .toList();
        List<String> shipmentIds = nodeIds.isEmpty()
                ? List.of()
                : shipmentRepository.findByStatusInAndOriginNodeIdInOrStatusInAndDestinationNodeIdInOrderByIdDesc(
                List.of(ShipmentStatus.READY, ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELAYED),
                nodeIds,
                List.of(ShipmentStatus.READY, ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELAYED),
                nodeIds
        ).stream().map(shipment -> shipment.getPublicId()).distinct().toList();

        List<String> settlementIds = settlementRepository.findReadablePublicIdsByOrganizationPublicIdAndStatus(
                organizationPublicId,
                SettlementStatus.PENDING
        );

        List<String> returnIds = returnRequestRepository.findByReturnStatusInAndRequestOrganizationPublicIdOrReturnStatusInAndTargetOrganizationPublicId(
                List.of(ReturnStatus.REQUESTED, ReturnStatus.APPROVED, ReturnStatus.IN_TRANSIT, ReturnStatus.RECEIVED),
                organizationPublicId,
                List.of(ReturnStatus.REQUESTED, ReturnStatus.APPROVED, ReturnStatus.IN_TRANSIT, ReturnStatus.RECEIVED),
                organizationPublicId
        ).stream().map(request -> request.getPublicId()).distinct().toList();

        List<String> certificateIds = supplierCertificateRepository.findReadablePublicIdsByOrganizationPublicIdAndStatuses(
                organizationPublicId,
                List.of(CertificateStatus.REVIEW_REQUESTED, CertificateStatus.EXPIRED, CertificateStatus.REVOKED)
        );

        return SupplySidebarBadgesResponse.builder()
                .ordersDesk(unreadCount(userPublicId, organizationPublicId, "ordersDesk", orderIds))
                .supplierControl(unreadCount(userPublicId, organizationPublicId, "supplierControl", supplierIds))
                .items(unreadCount(userPublicId, organizationPublicId, "items", itemIds))
                .inventory(unreadCount(userPublicId, organizationPublicId, "inventory", inventoryIds))
                .logisticsNodes(unreadCount(userPublicId, organizationPublicId, "logisticsNodes", logisticsNodeIds))
                .shipments(unreadCount(userPublicId, organizationPublicId, "shipments", shipmentIds))
                .settlements(unreadCount(userPublicId, organizationPublicId, "settlements", settlementIds))
                .returns(unreadCount(userPublicId, organizationPublicId, "returns", returnIds))
                .certificateWatch(unreadCount(userPublicId, organizationPublicId, "certificateWatch", certificateIds))
                .build();
    }

    @Transactional
    public void markDetailViewed(
            String userPublicId,
            String organizationPublicId,
            String organizationType,
            String menuKey,
            String detailPublicId
    ) {
        if (isMissing(userPublicId) || isMissing(organizationPublicId) || "ADMIN".equalsIgnoreCase(organizationType)
                || isMissing(menuKey) || isMissing(detailPublicId)) {
            return;
        }

        detailViewStateRepository
                .findByUserPublicIdAndOrganizationPublicIdAndMenuKeyAndDetailPublicId(
                        userPublicId,
                        organizationPublicId,
                        menuKey,
                        detailPublicId
                )
                .ifPresentOrElse(
                        SupplyDetailViewState::touch,
                        () -> detailViewStateRepository.save(new SupplyDetailViewState(
                                userPublicId,
                                organizationPublicId,
                                menuKey,
                                detailPublicId
                        ))
                );
    }

    private long unreadCount(
            String userPublicId,
            String organizationPublicId,
            String menuKey,
            Iterable<String> detailPublicIds
    ) {
        List<String> ids = toNonBlankList(detailPublicIds);
        if (ids.isEmpty()) {
            return 0;
        }
        if (isMissing(userPublicId)) {
            return ids.size();
        }
        long readCount = detailViewStateRepository.countByUserPublicIdAndOrganizationPublicIdAndMenuKeyAndDetailPublicIdIn(
                userPublicId,
                organizationPublicId,
                menuKey,
                ids
        );
        return Math.max(ids.size() - readCount, 0);
    }

    private List<String> toNonBlankList(Iterable<String> values) {
        Set<String> ids = new LinkedHashSet<>();
        for (String value : values) {
            if (!isMissing(value)) {
                ids.add(value);
            }
        }
        return List.copyOf(ids);
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private SupplySidebarBadgesResponse emptyResponse() {
        return SupplySidebarBadgesResponse.builder()
                .ordersDesk(0)
                .supplierControl(0)
                .items(0)
                .inventory(0)
                .logisticsNodes(0)
                .shipments(0)
                .settlements(0)
                .returns(0)
                .certificateWatch(0)
                .build();
    }
}

package com.ozz.atlas.supply.kafka.context;

import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SupplyChainContextResolver {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;

    public SupplyChainContextResolver(
            PurchaseOrderRepository purchaseOrderRepository,
            SubPurchaseOrderRepository subPurchaseOrderRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.subPurchaseOrderRepository = subPurchaseOrderRepository;
    }

    public SupplyChainContext fromPurchaseOrder(SupplyPurchaseOrder purchaseOrder) {
        if (purchaseOrder == null) {
            return SupplyChainContext.empty();
        }

        return new SupplyChainContext(
                purchaseOrder.getPublicId(),
                purchaseOrder.getBuyerOrganizationPublicId(),
                purchaseOrder.getBuyerOrganizationPublicId(),
                supplierOrganizationPublicId(purchaseOrder.getSupplier()),
                null,
                null
        );
    }

    public SupplyChainContext fromSubPurchaseOrder(SupplySubPurchaseOrder subPurchaseOrder) {
        if (subPurchaseOrder == null) {
            return SupplyChainContext.empty();
        }

        SupplyPurchaseOrder parentPurchaseOrder = subPurchaseOrder.getParentPurchaseOrder();
        return new SupplyChainContext(
                parentPurchaseOrder.getPublicId(),
                parentPurchaseOrder.getBuyerOrganizationPublicId(),
                supplierOrganizationPublicId(parentPurchaseOrder.getSupplier()),
                supplierOrganizationPublicId(subPurchaseOrder.getSupplier()),
                parentPurchaseOrder.getPublicId(),
                subPurchaseOrder.getPublicId()
        );
    }

    public SupplyChainContext fromShipment(Shipment shipment) {
        if (shipment == null) {
            return SupplyChainContext.empty();
        }

        if (shipment.getSubPoId() != null) {
            return subPurchaseOrderRepository.findById(shipment.getSubPoId())
                    .map(this::fromSubPurchaseOrder)
                    .orElseGet(SupplyChainContext::empty);
        }

        if (StringUtils.hasText(shipment.getSubPurchaseOrderPublicId())) {
            return subPurchaseOrderRepository
                    .findByPublicIdAndSubPoStatusNot(shipment.getSubPurchaseOrderPublicId(), SubPoStatus.DELETED)
                    .map(this::fromSubPurchaseOrder)
                    .orElseGet(SupplyChainContext::empty);
        }

        if (shipment.getPoId() != null) {
            return purchaseOrderRepository.findById(shipment.getPoId())
                    .map(this::fromPurchaseOrder)
                    .orElseGet(SupplyChainContext::empty);
        }

        if (StringUtils.hasText(shipment.getPurchaseOrderPublicId())) {
            return purchaseOrderRepository
                    .findByPublicIdAndPoStatusNot(shipment.getPurchaseOrderPublicId(), PoStatus.DELETED)
                    .map(this::fromPurchaseOrder)
                    .orElseGet(SupplyChainContext::empty);
        }

        return SupplyChainContext.empty();
    }

    public SupplyChainContext fromLot(Lot lot) {
        if (lot == null || lot.getSourcePoItem() == null) {
            return SupplyChainContext.empty();
        }
        return fromPurchaseOrder(lot.getSourcePoItem().getPurchaseOrder());
    }

    public SupplyChainContext fromReturn(ReturnRequest returnRequest, Shipment shipment) {
        SupplyChainContext shipmentContext = fromShipment(shipment);
        if (returnRequest == null) {
            return shipmentContext;
        }

        if (StringUtils.hasText(shipmentContext.rootPurchaseOrderPublicId())) {
            return shipmentContext;
        }

        return new SupplyChainContext(
                null,
                null,
                returnRequest.getRequestOrganizationPublicId(),
                returnRequest.getTargetOrganizationPublicId(),
                null,
                null
        );
    }

    public SupplyChainContext fromSupplier(SupplySupplier supplier) {
        if (supplier == null) {
            return SupplyChainContext.empty();
        }

        return new SupplyChainContext(
                null,
                null,
                null,
                supplier.getOrganizationPublicId(),
                null,
                null
        );
    }

    private String supplierOrganizationPublicId(SupplySupplier supplier) {
        return supplier != null ? supplier.getOrganizationPublicId() : null;
    }
}

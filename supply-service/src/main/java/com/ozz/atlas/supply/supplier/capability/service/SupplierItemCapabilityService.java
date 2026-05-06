package com.ozz.atlas.supply.supplier.capability.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.inventory.domain.InventoryTransaction;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventory;
import com.ozz.atlas.supply.inventory.repository.InventoryTransactionRepository;
import com.ozz.atlas.supply.inventory.repository.SupplyItemInventoryRepository;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyType;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventPayload;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.dtos.CreateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.dtos.SupplierItemCapabilityResponse;
import com.ozz.atlas.supply.supplier.capability.dtos.UpdateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.exception.SupplierItemCapabilityErrorCode;
import com.ozz.atlas.supply.supplier.capability.exception.SupplierItemCapabilityException;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierItemCapabilityService {

    private final SupplierItemCapabilityRepository capabilityRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;
    private final SupplyItemInventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;

    public SupplierItemCapabilityResponse createCapability(
            String supplierPublicId,
            CreateSupplierItemCapabilityRequest request,
            String actorUserPublicId
    ) {
        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        SupplyItem item = getActiveItem(request.getItemPublicId());

        // supplier, item 모두 내부 FK 엔티티이므로 중복 체크는 id 기준으로 처리한다.
        if (capabilityRepository.existsBySupplier_IdAndItem_Id(supplier.getId(), item.getId())) {
            throw new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.CAPABILITY_ALREADY_EXISTS);
        }

        SupplySupplierItemCapability capability = SupplySupplierItemCapability.create(
                supplier,
                item,
                request.getLeadTimeDays(),
                request.getMonthlyCapacity(),
                request.getAvailableQty(),
                request.getMoq(),
                request.getQualityGrade(),
                request.getUnitPriceHint(),
                request.getValidFrom(),
                request.getPartialConfirmationAllowed()
        );


        SupplySupplierItemCapability savedCapability = capabilityRepository.save(capability);

        createInitialInventoryIfStockBased(savedCapability);

        appendInventoryShortageEventIfNeeded(savedCapability, actorUserPublicId);

        return SupplierItemCapabilityResponse.fromEntity(savedCapability);
    }

    @Transactional(readOnly = true)
    public List<SupplierItemCapabilityResponse> getCapabilities(String supplierPublicId) {
        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);

        return capabilityRepository.findAllBySupplier_IdOrderByItem_ItemNameAsc(supplier.getId())
                .stream()
                .map(SupplierItemCapabilityResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierItemCapabilityResponse getCapability(String supplierPublicId, String itemPublicId) {
        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        SupplyItem item = getActiveItem(itemPublicId);

        SupplySupplierItemCapability capability = capabilityRepository.findBySupplier_IdAndItem_Id(
                        supplier.getId(),
                        item.getId()
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.CAPABILITY_NOT_FOUND));

        return SupplierItemCapabilityResponse.fromEntity(capability);
    }

    public SupplierItemCapabilityResponse updateCapability(
            String supplierPublicId,
            String itemPublicId,
            UpdateSupplierItemCapabilityRequest request,
            String actorUserPublicId
    ) {
        if (isEmptyPatch(request)) {
            throw new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.EMPTY_PATCH_NOT_ALLOWED);
        }

        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        SupplyItem item = getActiveItem(itemPublicId);

        SupplySupplierItemCapability capability = capabilityRepository.findBySupplier_IdAndItem_Id(
                        supplier.getId(),
                        item.getId()
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.CAPABILITY_NOT_FOUND));

        boolean wasShortage = isInventoryShortage(capability);

        capability.update(
                request.getLeadTimeDays(),
                request.getMonthlyCapacity(),
                request.getAvailableQty(),
                request.getMoq(),
                request.getQualityGrade(),
                request.getUnitPriceHint(),
                request.getValidFrom(),
                request.getPartialConfirmationAllowed()
        );


        if (!wasShortage) {
            appendInventoryShortageEventIfNeeded(capability, actorUserPublicId);
        }

        return SupplierItemCapabilityResponse.fromEntity(capability);
    }

    private SupplySupplier getSupplierOrThrow(String supplierPublicId) {
        return supplierRepository.findByPublicIdAndSupplierStatusNot(
                        supplierPublicId,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.SUPPLIER_NOT_FOUND));
    }

    private SupplyItem getActiveItem(String itemPublicId) {
        return supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE, Status.DEACTIVE)
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.ITEM_NOT_FOUND));
    }


    private boolean isEmptyPatch(UpdateSupplierItemCapabilityRequest request) {
        return request.getLeadTimeDays() == null
                && request.getMonthlyCapacity() == null
                && request.getAvailableQty() == null
                && request.getMoq() == null
                && request.getQualityGrade() == null
                && request.getUnitPriceHint() == null
                && request.getValidFrom() == null
                && request.getPartialConfirmationAllowed() == null;
    }
    
    private void appendInventoryShortageEventIfNeeded(
            SupplySupplierItemCapability capability,
            String actorUserPublicId
    ) {
        if (!isInventoryShortage(capability)) {
            return;
        }

        SupplySupplier supplier = capability.getSupplier();
        SupplyItem item = capability.getItem();
        SupplyChainContext context = supplyChainContextResolver.fromSupplier(supplier);
        SupplyDomainEventPayload payload = supplyDomainEventFactory.payload(
                item.getPublicId(),
                item.getItemCode(),
                "SHORTAGE",
                "재고 부족 감지",
                "%s 현재 공급 가능 수량 %d개가 최소 주문 수량 %d개 이하입니다.".formatted(
                        item.getItemName(),
                        capability.getAvailableQty(),
                        capability.getMoq()
                ),
                null
        );

        outboxEventAppender.append(supplyDomainEventFactory.create(
                KafkaTopics.SUPPLY_INVENTORY,
                EventTypes.INVENTORY_SHORTAGE_DETECTED,
                AggregateType.INVENTORY,
                item.getPublicId(),
                actorUserPublicId,
                supplier.getOrganizationPublicId(),
                context,
                payload
        ));
    }

    private boolean isInventoryShortage(SupplySupplierItemCapability capability) {
        return capability.getAvailableQty() != null
                && capability.getMoq() != null
                && capability.getAvailableQty() <= capability.getMoq();
    }

    private void createInitialInventoryIfStockBased(SupplySupplierItemCapability capability) {
        SupplyItem item = capability.getItem();

        if (item.getSupplyType() != SupplyType.STOCK_BASED) {
            return;
        }

        if (capability.getAvailableQty() == null || capability.getAvailableQty() <= 0) {
            return;
        }

        if (item.getOriginLogisticsNode() == null) {
            return;
        }

        java.time.LocalDate manufacturedDate = java.time.LocalDate.now();
        java.time.LocalDate expirationDate = manufacturedDate.plusDays(item.getShelfLifeDays());

        SupplyItemInventory inventory = SupplyItemInventory.create(
                capability.getSupplier(),
                item,
                item.getOriginLogisticsNode(),
                manufacturedDate,
                expirationDate,
                capability.getAvailableQty(),
                "Initial stock from item capability"
        );

        SupplyItemInventory savedInventory = inventoryRepository.save(inventory);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .inventoryId(savedInventory.getInventoryId())
                .itemPublicId(item.getPublicId())
                .reason(InventoryTransaction.TransactionReason.INITIAL_STOCK)
                .quantityChange(capability.getAvailableQty())
                .referenceId(null)
                .build();

        transactionRepository.save(transaction);
    }

}

package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.common.code.SequenceCodeType;
import com.ozz.atlas.supply.common.code.YearlySequenceCodeGenerator;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.kafka.shipment.ShipmentFactory;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentLine;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatusHistory;
import com.ozz.atlas.supply.shipment.dtos.CreateShipmentLineRequestDto;
import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentCreatableOrderDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentCreatableOrderItemDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentOriginNodeOptionDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.ShipmentLineRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentStatusHistoryRepository;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipmentCreateService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentLineRepository shipmentLineRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final ShipmentSearchService shipmentSearchService;
    private final OutboxEventAppender outboxEventAppender;
    private final ShipmentFactory shipmentFactory;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final SupplyChainContextResolver supplyChainContextResolver;
    private final ShipmentAuthorizationService shipmentAuthorizationService;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentInventoryService shipmentInventoryService;

    public ShipmentCreateService(
            ShipmentRepository shipmentRepository,
            ShipmentLineRepository shipmentLineRepository,
            ShipmentStatusHistoryRepository shipmentStatusHistoryRepository,
            LogisticsNodeRepository logisticsNodeRepository,
            ShipmentSearchService shipmentSearchService,
            OutboxEventAppender outboxEventAppender,
            ShipmentFactory shipmentFactory,
            PurchaseOrderRepository purchaseOrderRepository,
            SubPurchaseOrderRepository subPurchaseOrderRepository,
            SupplyChainContextResolver supplyChainContextResolver,
            ShipmentAuthorizationService shipmentAuthorizationService,
            ShipmentMapper shipmentMapper,
            ShipmentInventoryService shipmentInventoryService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentLineRepository = shipmentLineRepository;
        this.shipmentStatusHistoryRepository = shipmentStatusHistoryRepository;
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.shipmentSearchService = shipmentSearchService;
        this.outboxEventAppender = outboxEventAppender;
        this.shipmentFactory = shipmentFactory;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.subPurchaseOrderRepository = subPurchaseOrderRepository;
        this.supplyChainContextResolver = supplyChainContextResolver;
        this.shipmentAuthorizationService = shipmentAuthorizationService;
        this.shipmentMapper = shipmentMapper;
        this.shipmentInventoryService = shipmentInventoryService;
    }

    public ShipmentResponseDto createShipment(
            CreateShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateCreateShipmentActor(organizationPublicId, organizationType, userRole);

        if (dto.getShipmentLines() != null && !dto.getShipmentLines().isEmpty()) {
            return createOrderShipmentWithLines(dto, actorUserPublicId, organizationPublicId);
        }

        return createLegacyOrderShipment(dto, actorUserPublicId, organizationPublicId);
    }

    @Transactional(readOnly = true)
    public List<ShipmentCreatableOrderDto> getCreatableOrders(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateCreateShipmentActor(organizationPublicId, organizationType, userRole);

        return purchaseOrderRepository
                .findAllBySupplier_OrganizationPublicIdAndPoStatusNot(organizationPublicId, PoStatus.DELETED)
                .stream()
                .filter(purchaseOrder -> isShippablePurchaseOrderStatus(purchaseOrder.getPoStatus()))
                .map(this::toCreatableOrderDto)
                .filter(order -> order.getItems() != null && !order.getItems().isEmpty())
                .toList();
    }

    private ShipmentResponseDto createLegacyOrderShipment(
            CreateShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        ResolvedShipmentOrder order = resolveShipmentOrder(dto, organizationPublicId);

        if (dto.getDepartureEta() == null) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        LogisticsNode originNode = getActiveNode(dto.getOriginNodePublicId());
        LogisticsNode destinationNode = getActiveNode(order.destinationNodeId());

        if (!originNode.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_CREATION_NOT_ALLOWED);
        }

        if (!destinationNode.getOrganizationPublicId().equals(order.buyerOrganizationPublicId())) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_CREATION_NOT_ALLOWED);
        }

        Shipment shipment = Shipment.builder()
                .shipmentNumber(generateNextShipmentNumber())
                .poId(order.poId())
                .purchaseOrderPublicId(order.purchaseOrderPublicId())
                .subPoId(order.subPoId())
                .subPurchaseOrderPublicId(order.subPurchaseOrderPublicId())
                .sourceType(ShipmentSourceType.ORDER)
                .sourcePublicId(resolveOrderSourcePublicId(order))
                .originNodeId(originNode.getId())
                .destinationNodeId(destinationNode.getId())
                .currentNodeId(originNode.getId())
                .departureEta(dto.getDepartureEta())
                .arrivalEta(null)
                .status(ShipmentStatus.READY)
                .temperatureRequired(dto.isTemperatureRequired())
                .sealedPackagingRequired(dto.isSealedPackagingRequired())
                .fragile(dto.isFragile())
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        saveCreatedHistory(savedShipment, originNode, actorUserPublicId);
        publishCreatedEvent(savedShipment, originNode, destinationNode, actorUserPublicId, organizationPublicId);

        return shipmentMapper.toShipmentResponseDto(savedShipment);
    }

    private ShipmentResponseDto createOrderShipmentWithLines(
            CreateShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        if (dto.getSubPoId() != null || hasText(dto.getSubPurchaseOrderPublicId())) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        if (dto.getDepartureEta() == null) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        SupplyPurchaseOrder purchaseOrder = resolvePurchaseOrderForShipment(dto, organizationPublicId);
        Map<String, Long> requestQuantityMap = normalizeShipmentLineRequests(dto.getShipmentLines());
        Map<String, SupplyPurchaseOrderItem> orderItemMap = getConfirmedOrderItemMap(purchaseOrder);

        List<SupplyPurchaseOrderItem> selectedItems = new ArrayList<>();
        for (Map.Entry<String, Long> entry : requestQuantityMap.entrySet()) {
            SupplyPurchaseOrderItem orderItem = orderItemMap.get(entry.getKey());
            if (orderItem == null) {
                throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
            }

            if (entry.getValue() > getShippableQty(orderItem)) {
                throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
            }

            selectedItems.add(orderItem);
        }

        LogisticsNode destinationNode = resolveSingleDestinationNode(selectedItems);
        LogisticsNode originNode = resolveCommonOriginNode(
                dto,
                purchaseOrder,
                selectedItems,
                requestQuantityMap,
                organizationPublicId
        );

        Shipment shipment = Shipment.builder()
                .shipmentNumber(generateNextShipmentNumber())
                .poId(purchaseOrder.getId())
                .purchaseOrderPublicId(purchaseOrder.getPublicId())
                .sourceType(ShipmentSourceType.ORDER)
                .sourcePublicId(purchaseOrder.getPublicId())
                .originNodeId(originNode.getId())
                .destinationNodeId(destinationNode.getId())
                .currentNodeId(originNode.getId())
                .departureEta(dto.getDepartureEta())
                .arrivalEta(null)
                .status(ShipmentStatus.READY)
                .temperatureRequired(dto.isTemperatureRequired())
                .sealedPackagingRequired(dto.isSealedPackagingRequired())
                .fragile(dto.isFragile())
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        for (SupplyPurchaseOrderItem orderItem : selectedItems) {
            Long quantity = requestQuantityMap.get(orderItem.getPublicId());
            ShipmentLine shipmentLine = ShipmentLine.builder()
                    .shipmentId(savedShipment.getId())
                    .sourceType(ShipmentSourceType.ORDER)
                    .sourcePublicId(purchaseOrder.getPublicId())
                    .sourceItemPublicId(orderItem.getPublicId())
                    .itemPublicId(orderItem.getItem().getPublicId())
                    .itemCode(orderItem.getItem().getItemCode())
                    .itemName(orderItem.getItem().getItemName())
                    .quantity(quantity)
                    .originNodeId(originNode.getId())
                    .destinationNodeId(destinationNode.getId())
                    .build();

            ShipmentLine savedLine = shipmentLineRepository.save(shipmentLine);
            shipmentInventoryService.deductReservedForShipmentLine(
                    purchaseOrder.getSupplier(),
                    orderItem.getItem(),
                    originNode.getId(),
                    savedLine
            );
        }

        saveCreatedHistory(savedShipment, originNode, actorUserPublicId);
        publishCreatedEvent(savedShipment, originNode, destinationNode, actorUserPublicId, organizationPublicId);

        return shipmentMapper.toShipmentResponseDto(savedShipment);
    }

    private void saveCreatedHistory(
            Shipment shipment,
            LogisticsNode originNode,
            String actorUserPublicId
    ) {
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipmentId(shipment.getId())
                .statusCode(shipment.getStatus())
                .statusMessage("출하 생성")
                .locationText(originNode.getNodeName())
                .latitude(originNode.getLatitude())
                .longitude(originNode.getLongitude())
                .recordedAt(LocalDateTime.now())
                .recordedBy(actorUserPublicId != null ? actorUserPublicId : "SYSTEM")
                .build();

        shipmentStatusHistoryRepository.save(history);
    }

    private void publishCreatedEvent(
            Shipment shipment,
            LogisticsNode originNode,
            LogisticsNode destinationNode,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        shipmentSearchService.saveShipmentDocument(shipment);

        outboxEventAppender.append(
                shipmentFactory.createShipmentCreatedEvent(
                        shipment,
                        originNode.getPublicId(),
                        destinationNode.getPublicId(),
                        supplyChainContextResolver.fromShipment(shipment),
                        actorUserPublicId,
                        organizationPublicId
                )
        );
    }

    private Map<String, Long> normalizeShipmentLineRequests(List<CreateShipmentLineRequestDto> requestLines) {
        if (requestLines == null || requestLines.isEmpty()) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        Map<String, Long> quantityMap = new LinkedHashMap<>();
        for (CreateShipmentLineRequestDto requestLine : requestLines) {
            if (requestLine == null
                    || !hasText(requestLine.getSourceItemPublicId())
                    || requestLine.getQuantity() == null
                    || requestLine.getQuantity() <= 0) {
                throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
            }

            quantityMap.merge(
                    requestLine.getSourceItemPublicId(),
                    requestLine.getQuantity(),
                    Long::sum
            );
        }

        return quantityMap;
    }

    private Map<String, SupplyPurchaseOrderItem> getConfirmedOrderItemMap(SupplyPurchaseOrder purchaseOrder) {
        return purchaseOrder.getActiveItems().stream()
                .filter(this::isConfirmedOrderItem)
                .collect(Collectors.toMap(
                        SupplyPurchaseOrderItem::getPublicId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private boolean isConfirmedOrderItem(SupplyPurchaseOrderItem item) {
        return item.getItemStatus() == PurchaseOrderItemStatus.CONFIRMED
                || item.getItemStatus() == PurchaseOrderItemStatus.PARTIALLY_CONFIRMED;
    }

    private Long getShippableQty(SupplyPurchaseOrderItem orderItem) {
        Long confirmedQty = orderItem.getConfirmedQty() != null ? orderItem.getConfirmedQty() : 0L;
        Long alreadyShipmentQty = shipmentLineRepository.sumQuantityBySourceItemPublicIdAndShipmentStatusIn(
                orderItem.getPublicId(),
                activeShipmentStatuses()
        );

        return Math.max(confirmedQty - (alreadyShipmentQty != null ? alreadyShipmentQty : 0L), 0L);
    }

    private List<ShipmentStatus> activeShipmentStatuses() {
        return List.of(
                ShipmentStatus.READY,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.DELAYED,
                ShipmentStatus.ARRIVED
        );
    }

    private LogisticsNode resolveSingleDestinationNode(List<SupplyPurchaseOrderItem> selectedItems) {
        Set<Long> destinationNodeIds = selectedItems.stream()
                .map(SupplyPurchaseOrderItem::getArrivalLogisticsNode)
                .filter(Objects::nonNull)
                .map(LogisticsNode::getId)
                .collect(Collectors.toSet());

        if (destinationNodeIds.size() != 1) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        return getActiveNode(destinationNodeIds.iterator().next());
    }

    private LogisticsNode resolveCommonOriginNode(
            CreateShipmentRequestDto dto,
            SupplyPurchaseOrder purchaseOrder,
            List<SupplyPurchaseOrderItem> selectedItems,
            Map<String, Long> requestQuantityMap,
            String organizationPublicId
    ) {
        if (hasText(dto.getOriginNodePublicId())) {
            LogisticsNode requestedNode = getActiveNode(dto.getOriginNodePublicId());
            if (!requestedNode.getOrganizationPublicId().equals(organizationPublicId)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }

            if (!canShipAllItemsFromNode(purchaseOrder, selectedItems, requestQuantityMap, requestedNode)) {
                throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
            }

            return requestedNode;
        }

        return logisticsNodeRepository.findByOrganizationPublicId(organizationPublicId).stream()
                .filter(LogisticsNode::isActive)
                .filter(node -> canShipAllItemsFromNode(purchaseOrder, selectedItems, requestQuantityMap, node))
                .sorted(Comparator.comparing(LogisticsNode::getNodeCode))
                .findFirst()
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
    }

    private boolean canShipAllItemsFromNode(
            SupplyPurchaseOrder purchaseOrder,
            List<SupplyPurchaseOrderItem> selectedItems,
            Map<String, Long> requestQuantityMap,
            LogisticsNode node
    ) {
        for (SupplyPurchaseOrderItem selectedItem : selectedItems) {
            Long requestedQty = requestQuantityMap.get(selectedItem.getPublicId());
            Long reservedQty = shipmentInventoryService.getReservedQtyByNode(
                    purchaseOrder.getSupplier(),
                    selectedItem.getItem(),
                    node.getId()
            );

            if (reservedQty == null || reservedQty < requestedQty) {
                return false;
            }
        }

        return true;
    }

    private ShipmentCreatableOrderDto toCreatableOrderDto(SupplyPurchaseOrder purchaseOrder) {
        List<ShipmentCreatableOrderItemDto> items = purchaseOrder.getActiveItems().stream()
                .filter(this::isConfirmedOrderItem)
                .map(item -> toCreatableOrderItemDto(purchaseOrder, item))
                .filter(Objects::nonNull)
                .toList();

        return ShipmentCreatableOrderDto.builder()
                .sourceType(ShipmentSourceType.ORDER)
                .sourcePublicId(purchaseOrder.getPublicId())
                .orderNumber(purchaseOrder.getPoNumber())
                .buyerOrganizationPublicId(purchaseOrder.getBuyerOrganizationPublicId())
                .supplierPublicId(purchaseOrder.getSupplier().getPublicId())
                .supplierName(purchaseOrder.getSupplier().getSupplierName())
                .status(purchaseOrder.getPoStatus().name())
                .items(items)
                .build();
    }

    private ShipmentCreatableOrderItemDto toCreatableOrderItemDto(
            SupplyPurchaseOrder purchaseOrder,
            SupplyPurchaseOrderItem orderItem
    ) {
        Long shippableQty = getShippableQty(orderItem);
        if (shippableQty <= 0) {
            return null;
        }

        LogisticsNode destinationNode = orderItem.getArrivalLogisticsNode();
        if (destinationNode == null || !destinationNode.isActive()) {
            return null;
        }

        List<ShipmentOriginNodeOptionDto> originNodeOptions = getOriginNodeOptions(
                purchaseOrder,
                orderItem,
                shippableQty
        );
        if (originNodeOptions.isEmpty()) {
            return null;
        }

        Long confirmedQty = orderItem.getConfirmedQty() != null ? orderItem.getConfirmedQty() : 0L;

        return ShipmentCreatableOrderItemDto.builder()
                .sourceItemPublicId(orderItem.getPublicId())
                .itemPublicId(orderItem.getItem().getPublicId())
                .itemCode(orderItem.getItem().getItemCode())
                .itemName(orderItem.getItem().getItemName())
                .confirmedQty(confirmedQty)
                .alreadyShipmentQty(confirmedQty - shippableQty)
                .shippableQty(shippableQty)
                .destinationNodePublicId(destinationNode.getPublicId())
                .destinationNodeCode(destinationNode.getNodeCode())
                .destinationNodeName(destinationNode.getNodeName())
                .originNodeOptions(originNodeOptions)
                .build();
    }

    private List<ShipmentOriginNodeOptionDto> getOriginNodeOptions(
            SupplyPurchaseOrder purchaseOrder,
            SupplyPurchaseOrderItem orderItem,
            Long shippableQty
    ) {
        return logisticsNodeRepository.findByOrganizationPublicId(
                        purchaseOrder.getSupplier().getOrganizationPublicId()
                )
                .stream()
                .filter(LogisticsNode::isActive)
                .map(node -> toOriginNodeOption(purchaseOrder, orderItem, shippableQty, node))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ShipmentOriginNodeOptionDto::getNodeCode))
                .toList();
    }

    private ShipmentOriginNodeOptionDto toOriginNodeOption(
            SupplyPurchaseOrder purchaseOrder,
            SupplyPurchaseOrderItem orderItem,
            Long shippableQty,
            LogisticsNode node
    ) {
        Long reservedQty = shipmentInventoryService.getReservedQtyByNode(
                purchaseOrder.getSupplier(),
                orderItem.getItem(),
                node.getId()
        );

        if (reservedQty == null || reservedQty <= 0) {
            return null;
        }

        return ShipmentOriginNodeOptionDto.builder()
                .nodePublicId(node.getPublicId())
                .nodeCode(node.getNodeCode())
                .nodeName(node.getNodeName())
                .availableQty(Math.min(reservedQty, shippableQty))
                .build();
    }

    private ResolvedShipmentOrder resolveShipmentOrder(CreateShipmentRequestDto dto, String organizationPublicId) {
        if (dto.getSubPoId() != null || hasText(dto.getSubPurchaseOrderPublicId())) {
            SupplySubPurchaseOrder subPurchaseOrder = resolveSubPurchaseOrderForShipment(dto, organizationPublicId);

            return new ResolvedShipmentOrder(
                    null,
                    null,
                    subPurchaseOrder.getSubPoId(),
                    subPurchaseOrder.getPublicId(),
                    subPurchaseOrder.getParentPurchaseOrder().getSupplier().getOrganizationPublicId(),
                    subPurchaseOrder.getSubPoNumber(),
                    resolveSubPurchaseOrderDestinationNodeId(subPurchaseOrder)
            );
        }

        SupplyPurchaseOrder purchaseOrder = resolvePurchaseOrderForShipment(dto, organizationPublicId);

        return new ResolvedShipmentOrder(
                purchaseOrder.getId(),
                purchaseOrder.getPublicId(),
                null,
                null,
                purchaseOrder.getBuyerOrganizationPublicId(),
                purchaseOrder.getPoNumber(),
                resolvePurchaseOrderDestinationNodeId(purchaseOrder)
        );
    }

    private SupplyPurchaseOrder resolvePurchaseOrderForShipment(
            CreateShipmentRequestDto dto,
            String organizationPublicId
    ) {
        SupplyPurchaseOrder purchaseOrder;

        if (dto.getPoId() != null) {
            purchaseOrder = purchaseOrderRepository.findById(dto.getPoId())
                    .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
        } else if (hasText(dto.getPurchaseOrderPublicId())) {
            purchaseOrder = purchaseOrderRepository.findByPublicIdAndPoStatusNot(
                            dto.getPurchaseOrderPublicId(),
                            PoStatus.DELETED
                    )
                    .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
        } else {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        if (!purchaseOrder.getSupplier().getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        if (!isShippablePurchaseOrderStatus(purchaseOrder.getPoStatus())) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_ORDER_STATUS_NOT_ALLOWED);
        }

        return purchaseOrder;
    }

    private SupplySubPurchaseOrder resolveSubPurchaseOrderForShipment(
            CreateShipmentRequestDto dto,
            String organizationPublicId
    ) {
        SupplySubPurchaseOrder subPurchaseOrder;

        if (dto.getSubPoId() != null) {
            subPurchaseOrder = subPurchaseOrderRepository.findById(dto.getSubPoId())
                    .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
        } else if (hasText(dto.getSubPurchaseOrderPublicId())) {
            subPurchaseOrder = subPurchaseOrderRepository.findByPublicIdAndSubPoStatusNot(
                            dto.getSubPurchaseOrderPublicId(),
                            SubPoStatus.DELETED
                    )
                    .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
        } else {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        if (!subPurchaseOrder.getSupplier().getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        if (!isShippableSubPurchaseOrderStatus(subPurchaseOrder.getSubPoStatus())) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_ORDER_STATUS_NOT_ALLOWED);
        }

        return subPurchaseOrder;
    }

    private Long resolvePurchaseOrderDestinationNodeId(SupplyPurchaseOrder purchaseOrder) {
        Set<Long> destinationNodeIds = purchaseOrder.getActiveItems().stream()
                .filter(item -> item.getItemStatus() == PurchaseOrderItemStatus.CONFIRMED
                        || item.getItemStatus() == PurchaseOrderItemStatus.PARTIALLY_CONFIRMED)
                .map(SupplyPurchaseOrderItem::getArrivalLogisticsNode)
                .filter(Objects::nonNull)
                .map(LogisticsNode::getId)
                .collect(Collectors.toSet());

        if (destinationNodeIds.size() != 1) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        return destinationNodeIds.iterator().next();
    }

    private Long resolveSubPurchaseOrderDestinationNodeId(SupplySubPurchaseOrder subPurchaseOrder) {
        Set<Long> destinationNodeIds = subPurchaseOrder.getActiveItems().stream()
                .filter(item -> item.getLineStatus() == com.ozz.atlas.supply.subpurchaseorder.domain.SubPurchaseOrderLineStatus.CONFIRMED
                        || item.getLineStatus() == com.ozz.atlas.supply.subpurchaseorder.domain.SubPurchaseOrderLineStatus.PARTIALLY_CONFIRMED)
                .map(item -> item.getParentPurchaseOrderItem().getArrivalLogisticsNode())
                .filter(Objects::nonNull)
                .map(LogisticsNode::getId)
                .collect(Collectors.toSet());

        if (destinationNodeIds.size() != 1) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        return destinationNodeIds.iterator().next();
    }

    private String generateNextShipmentNumber() {
        String prefix = YearlySequenceCodeGenerator.currentPrefix(SequenceCodeType.SHIPMENT);
        String lastShipmentNumber = shipmentRepository
                .findTopByShipmentNumberStartingWithOrderByShipmentNumberDesc(prefix)
                .map(Shipment::getShipmentNumber)
                .orElse(null);

        String candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.SHIPMENT, lastShipmentNumber, 7);
        while (shipmentRepository.existsByShipmentNumber(candidate)) {
            candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.SHIPMENT, candidate, 7);
        }

        return candidate;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveOrderSourcePublicId(ResolvedShipmentOrder order) {
        return hasText(order.subPurchaseOrderPublicId())
                ? order.subPurchaseOrderPublicId()
                : order.purchaseOrderPublicId();
    }

    private boolean isShippablePurchaseOrderStatus(PoStatus poStatus) {
        return poStatus == PoStatus.PARTIALLY_CONFIRMED
                || poStatus == PoStatus.CONFIRMED;
    }

    private boolean isShippableSubPurchaseOrderStatus(SubPoStatus subPoStatus) {
        return subPoStatus == SubPoStatus.PARTIALLY_CONFIRMED
                || subPoStatus == SubPoStatus.CONFIRMED;
    }

    private LogisticsNode getActiveNode(Long nodeId) {
        LogisticsNode node = logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));

        if (!node.isActive()) {
            throw new ShipmentException(ShipmentErrorCode.INACTIVE_LOGISTICS_NODE);
        }

        return node;
    }

    private LogisticsNode getActiveNode(String publicId) {
        LogisticsNode node = logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));

        if (!node.isActive()) {
            throw new ShipmentException(ShipmentErrorCode.INACTIVE_LOGISTICS_NODE);
        }

        return node;
    }

    private record ResolvedShipmentOrder(
            Long poId,
            String purchaseOrderPublicId,
            Long subPoId,
            String subPurchaseOrderPublicId,
            String buyerOrganizationPublicId,
            String orderNumber,
            Long destinationNodeId
    ) {
    }
}

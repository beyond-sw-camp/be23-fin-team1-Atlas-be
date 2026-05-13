package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.dtos.ShipmentLineResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.repository.ShipmentLineRepository;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import org.springframework.stereotype.Component;
import com.ozz.atlas.supply.shipment.dtos.ShipmentListResponseDto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ShipmentMapper {

    private final LogisticsNodeRepository logisticsNodeRepository;
    private final ShipmentLineRepository shipmentLineRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;

    public ShipmentMapper(
            LogisticsNodeRepository logisticsNodeRepository,
            ShipmentLineRepository shipmentLineRepository,
            ReturnRequestRepository returnRequestRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SubPurchaseOrderRepository subPurchaseOrderRepository
    ) {
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.shipmentLineRepository = shipmentLineRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.subPurchaseOrderRepository = subPurchaseOrderRepository;
    }

    public ShipmentResponseDto toShipmentResponseDto(Shipment shipment) {
        return toShipmentResponseDto(shipment, null);
    }

    public ShipmentResponseDto toShipmentResponseDto(Shipment shipment, String organizationPublicId) {
        Map<Long, LogisticsNode> nodeMap = getShipmentNodeMap(java.util.List.of(shipment));

        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());
        ShipmentActionPermissions permissions = resolveActionPermissions(
                shipment,
                originNode,
                destinationNode,
                organizationPublicId
        );
        String purchaseOrderNumber = resolvePurchaseOrderNumber(shipment);
        String subPurchaseOrderNumber = resolveSubPurchaseOrderNumber(shipment);
        String sourceNumber = resolveSourceNumber(shipment, purchaseOrderNumber, subPurchaseOrderNumber);

        return ShipmentResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .sourceType(resolveSourceType(shipment))
                .sourcePublicId(shipment.getSourcePublicId())
                .sourceNumber(sourceNumber)
                .poId(shipment.getPoId())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .purchaseOrderNumber(purchaseOrderNumber)
                .subPoId(shipment.getSubPoId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
                .subPurchaseOrderNumber(subPurchaseOrderNumber)
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .originNodePublicId(originNode != null ? originNode.getPublicId() : null)
                .originNodeName(originNode != null ? originNode.getNodeName() : null)
                .originNodeCode(originNode != null ? originNode.getNodeCode() : null)
                .originLatitude(originNode != null ? originNode.getLatitude() : null)
                .originLongitude(originNode != null ? originNode.getLongitude() : null)
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .destinationNodeCode(destinationNode != null ? destinationNode.getNodeCode() : null)
                .destinationLatitude(destinationNode != null ? destinationNode.getLatitude() : null)
                .destinationLongitude(destinationNode != null ? destinationNode.getLongitude() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
                .currentNodeCode(currentNode != null ? currentNode.getNodeCode() : null)
                .currentLatitude(currentNode != null ? currentNode.getLatitude() : null)
                .currentLongitude(currentNode != null ? currentNode.getLongitude() : null)
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .sealedPackagingRequired(shipment.isSealedPackagingRequired())
                .fragile(shipment.isFragile())
                .canUpdate(permissions.canUpdate())
                .canStart(permissions.canStart())
                .canArrive(permissions.canArrive())
                .canCancel(permissions.canCancel())
                .canTrack(permissions.canTrack())
                .canRegisterException(permissions.canRegisterException())
                .hasReturn(returnRequestRepository.existsBySourceShipmentPublicId(shipment.getPublicId()))
                .shipmentLines(getShipmentLineResponses(shipment))
                .build();

    }
    public ShipmentListResponseDto toShipmentListResponseDto(Shipment shipment) {
        return toShipmentListResponseDto(shipment, null);
    }

    public ShipmentListResponseDto toShipmentListResponseDto(Shipment shipment, String organizationPublicId) {
        Map<Long, LogisticsNode> nodeMap = getShipmentNodeMap(java.util.List.of(shipment));

        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());
        ShipmentActionPermissions permissions = resolveActionPermissions(
                shipment,
                originNode,
                destinationNode,
                organizationPublicId
        );

        return ShipmentListResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .sourceType(resolveSourceType(shipment))
                .sourcePublicId(shipment.getSourcePublicId())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
                .carrierName(shipment.getCarrierName())
                .originNodePublicId(originNode != null ? originNode.getPublicId() : null)
                .originNodeName(originNode != null ? originNode.getNodeName() : null)
                .originNodeCode(originNode != null ? originNode.getNodeCode() : null)
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .destinationNodeCode(destinationNode != null ? destinationNode.getNodeCode() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
                .currentNodeCode(currentNode != null ? currentNode.getNodeCode() : null)
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .sealedPackagingRequired(shipment.isSealedPackagingRequired())
                .fragile(shipment.isFragile())
                .canUpdate(permissions.canUpdate())
                .canStart(permissions.canStart())
                .canArrive(permissions.canArrive())
                .canCancel(permissions.canCancel())
                .canTrack(permissions.canTrack())
                .canRegisterException(permissions.canRegisterException())
                .hasReturn(returnRequestRepository.existsBySourceShipmentPublicId(shipment.getPublicId()))
                .build();
    }

    private Map<Long, LogisticsNode> getShipmentNodeMap(Collection<Shipment> shipments) {
        if (shipments == null || shipments.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> nodeIds = shipments.stream()
                .flatMap(shipment -> Stream.of(
                        shipment.getOriginNodeId(),
                        shipment.getDestinationNodeId(),
                        shipment.getCurrentNodeId()
                ))
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return logisticsNodeRepository.findByIdIn(nodeIds).stream()
                .collect(Collectors.toMap(LogisticsNode::getId, node -> node));
    }

    private ShipmentSourceType resolveSourceType(Shipment shipment) {
        return shipment.getSourceType() != null ? shipment.getSourceType() : ShipmentSourceType.ORDER;
    }

    private String resolvePurchaseOrderNumber(Shipment shipment) {
        if (shipment.getPoId() != null) {
            return purchaseOrderRepository.findById(shipment.getPoId())
                    .map(order -> order.getPoNumber())
                    .orElse(null);
        }

        if (shipment.getPurchaseOrderPublicId() != null && !shipment.getPurchaseOrderPublicId().isBlank()) {
            return purchaseOrderRepository.findByPublicIdAndPoStatusNot(
                            shipment.getPurchaseOrderPublicId(),
                            com.ozz.atlas.supply.purchaseorder.domain.PoStatus.DELETED
                    )
                    .map(order -> order.getPoNumber())
                    .orElse(null);
        }

        return null;
    }

    private String resolveSubPurchaseOrderNumber(Shipment shipment) {
        if (shipment.getSubPoId() != null) {
            return subPurchaseOrderRepository.findById(shipment.getSubPoId())
                    .map(order -> order.getSubPoNumber())
                    .orElse(null);
        }

        if (shipment.getSubPurchaseOrderPublicId() != null && !shipment.getSubPurchaseOrderPublicId().isBlank()) {
            return subPurchaseOrderRepository.findByPublicIdAndSubPoStatusNot(
                            shipment.getSubPurchaseOrderPublicId(),
                            com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus.DELETED
                    )
                    .map(order -> order.getSubPoNumber())
                    .orElse(null);
        }

        return null;
    }

    private String resolveSourceNumber(
            Shipment shipment,
            String purchaseOrderNumber,
            String subPurchaseOrderNumber
    ) {
        ShipmentSourceType sourceType = resolveSourceType(shipment);
        if ((sourceType == ShipmentSourceType.RETURN || sourceType == ShipmentSourceType.EXCHANGE)
                && shipment.getSourcePublicId() != null
                && !shipment.getSourcePublicId().isBlank()) {
            return returnRequestRepository.findByPublicId(shipment.getSourcePublicId())
                    .map(ReturnRequest::getReturnNumber)
                    .orElse(null);
        }

        if (subPurchaseOrderNumber != null) {
            return subPurchaseOrderNumber;
        }

        return purchaseOrderNumber;
    }

    private List<ShipmentLineResponseDto> getShipmentLineResponses(
            Shipment shipment
    ) {
        if (shipment == null || shipment.getId() == null) {
            return List.of();
        }

        return shipmentLineRepository.findByShipmentIdOrderByIdAsc(shipment.getId()).stream()
                .map(ShipmentLineResponseDto::from)
                .toList();
    }

    public ShipmentActionPermissions resolveActionPermissions(
            Shipment shipment,
            LogisticsNode originNode,
            LogisticsNode destinationNode,
            String organizationPublicId
    ) {
        if (shipment == null || organizationPublicId == null || organizationPublicId.isBlank()) {
            return ShipmentActionPermissions.none();
        }

        boolean isSender = originNode != null
                && organizationPublicId.equals(originNode.getOrganizationPublicId());
        boolean isReceiver = destinationNode != null
                && organizationPublicId.equals(destinationNode.getOrganizationPublicId());
        boolean isReady = shipment.getStatus() == ShipmentStatus.READY;
        boolean isInDelivery = shipment.getStatus() == ShipmentStatus.IN_TRANSIT
                || shipment.getStatus() == ShipmentStatus.DELAYED;

        return new ShipmentActionPermissions(
                isReady && isSender,
                isReady && isSender,
                isInDelivery && isReceiver,
                isReady && isSender,
                isInDelivery && isSender,
                isInDelivery && isSender
        );
    }

    public record ShipmentActionPermissions(
            boolean canUpdate,
            boolean canStart,
            boolean canArrive,
            boolean canCancel,
            boolean canTrack,
            boolean canRegisterException
    ) {
        static ShipmentActionPermissions none() {
            return new ShipmentActionPermissions(false, false, false, false, false, false);
        }
    }
}

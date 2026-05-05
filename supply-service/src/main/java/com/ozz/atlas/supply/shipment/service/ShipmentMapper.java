package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.dtos.ShipmentLineResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.repository.ShipmentLineRepository;
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

    public ShipmentMapper(
            LogisticsNodeRepository logisticsNodeRepository,
            ShipmentLineRepository shipmentLineRepository
    ) {
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.shipmentLineRepository = shipmentLineRepository;
    }

    public ShipmentResponseDto toShipmentResponseDto(Shipment shipment) {
        Map<Long, LogisticsNode> nodeMap = getShipmentNodeMap(java.util.List.of(shipment));

        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());

        return ShipmentResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .sourceType(resolveSourceType(shipment))
                .sourcePublicId(shipment.getSourcePublicId())
                .poId(shipment.getPoId())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .subPoId(shipment.getSubPoId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
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
                .shipmentLines(getShipmentLineResponses(shipment))
                .build();

    }
    public ShipmentListResponseDto toShipmentListResponseDto(Shipment shipment) {
        Map<Long, LogisticsNode> nodeMap = getShipmentNodeMap(java.util.List.of(shipment));

        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());

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
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .sealedPackagingRequired(shipment.isSealedPackagingRequired())
                .fragile(shipment.isFragile())
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
}

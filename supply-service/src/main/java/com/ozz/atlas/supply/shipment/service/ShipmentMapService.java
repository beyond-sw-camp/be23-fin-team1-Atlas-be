package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentCheckpoint;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.dtos.ShipmentMapCheckpointDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentMapResponseDto;
import com.ozz.atlas.supply.shipment.repository.ShipmentCheckpointRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ShipmentMapService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentCheckpointRepository shipmentCheckpointRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final ShipmentAuthorizationService shipmentAuthorizationService;
    private final ShipmentEtaCalculator shipmentEtaCalculator;

    public ShipmentMapService(
            ShipmentRepository shipmentRepository,
            ShipmentCheckpointRepository shipmentCheckpointRepository,
            LogisticsNodeRepository logisticsNodeRepository,
            ShipmentAuthorizationService shipmentAuthorizationService,
            ShipmentEtaCalculator shipmentEtaCalculator
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentCheckpointRepository = shipmentCheckpointRepository;
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.shipmentAuthorizationService = shipmentAuthorizationService;
        this.shipmentEtaCalculator = shipmentEtaCalculator;
    }

    public List<ShipmentMapResponseDto> getShipmentMapData(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Set<Long> myNodeIds = getOrganizationNodeIds(organizationPublicId);
        if (myNodeIds.isEmpty()) {
            return List.of();
        }

        List<ShipmentStatus> activeStatuses = List.of(
                ShipmentStatus.READY,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.DELAYED
        );

        List<Shipment> shipments =
                shipmentRepository.findByStatusInAndOriginNodeIdInOrStatusInAndDestinationNodeIdInOrderByIdDesc(
                        activeStatuses,
                        myNodeIds,
                        activeStatuses,
                        myNodeIds
                );

        if (shipments.isEmpty()) {
            return List.of();
        }

        List<Shipment> distinctShipments = dedupeShipments(shipments);

        List<Long> shipmentIds = distinctShipments.stream()
                .map(Shipment::getId)
                .toList();

        List<ShipmentCheckpoint> transitCheckpoints =
                shipmentCheckpointRepository.findByShipmentIdInAndCheckpointTypeOrderByActualAtAsc(
                        shipmentIds,
                        CheckpointType.TRANSIT
                );

        Map<Long, LogisticsNode> nodeMap = getShipmentMapNodeMap(distinctShipments, transitCheckpoints);
        Map<Long, List<ShipmentCheckpoint>> checkpointMap = transitCheckpoints.stream()
                .collect(java.util.stream.Collectors.groupingBy(ShipmentCheckpoint::getShipmentId));

        return distinctShipments.stream()
                .map(shipment -> toShipmentMapResponseDto(
                        shipment,
                        nodeMap,
                        checkpointMap.getOrDefault(shipment.getId(), List.of())
                ))
                .toList();
    }

    private List<Shipment> dedupeShipments(List<Shipment> shipments) {
        return shipments.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Shipment::getPublicId,
                        shipment -> shipment,
                        (first, second) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private Set<Long> getOrganizationNodeIds(String organizationPublicId) {
        return logisticsNodeRepository.findByOrganizationPublicId(organizationPublicId).stream()
                .map(LogisticsNode::getId)
                .collect(java.util.stream.Collectors.toSet());
    }

    private Map<Long, LogisticsNode> getShipmentMapNodeMap(
            Collection<Shipment> shipments,
            Collection<ShipmentCheckpoint> checkpoints
    ) {
        Set<Long> nodeIds = new HashSet<>();

        if (shipments != null) {
            shipments.stream()
                    .flatMap(shipment -> Stream.of(
                            shipment.getOriginNodeId(),
                            shipment.getDestinationNodeId(),
                            shipment.getCurrentNodeId()
                    ))
                    .filter(id -> id != null)
                    .forEach(nodeIds::add);
        }

        if (checkpoints != null) {
            checkpoints.stream()
                    .map(ShipmentCheckpoint::getNodeId)
                    .filter(id -> id != null)
                    .forEach(nodeIds::add);
        }

        if (nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return logisticsNodeRepository.findByIdIn(nodeIds).stream()
                .collect(java.util.stream.Collectors.toMap(LogisticsNode::getId, node -> node));
    }

    private ShipmentMapResponseDto toShipmentMapResponseDto(
            Shipment shipment,
            Map<Long, LogisticsNode> nodeMap,
            List<ShipmentCheckpoint> transitCheckpoints
    ) {
        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = resolveCurrentNode(shipment, nodeMap, originNode);

        ShipmentEtaCalculator.Result etaResult = shipmentEtaCalculator.calculate(shipment, transitCheckpoints);

        List<ShipmentMapCheckpointDto> checkpointDtos = transitCheckpoints.stream()
                .filter(checkpoint -> checkpoint.getCheckpointType() == CheckpointType.TRANSIT)
                .sorted(Comparator.comparing(
                        ShipmentCheckpoint::getActualAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(checkpoint -> toShipmentMapCheckpointDto(checkpoint, nodeMap.get(checkpoint.getNodeId())))
                .toList();

        return ShipmentMapResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .sourceType(resolveSourceType(shipment))
                .sourcePublicId(shipment.getSourcePublicId())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .status(shipment.getStatus())
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
                .estimatedArrivalAt(etaResult.getEstimatedArrivalAt())
                .delayed(etaResult.isDelayed())
                .delayMinutes(etaResult.getDelayMinutes())
                .etaBasis(etaResult.getEtaBasis())
                .lastCheckpointType(
                        etaResult.getLatestPassedCheckpoint() != null
                                ? etaResult.getLatestPassedCheckpoint().getCheckpointType()
                                : null
                )
                .lastCheckpointAt(
                        etaResult.getLatestPassedCheckpoint() != null
                                ? etaResult.getLatestPassedCheckpoint().getActualAt()
                                : null
                )
                .checkpoints(checkpointDtos)
                .build();
    }

    private LogisticsNode resolveCurrentNode(
            Shipment shipment,
            Map<Long, LogisticsNode> nodeMap,
            LogisticsNode originNode
    ) {
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());
        return currentNode != null ? currentNode : originNode;
    }

    private ShipmentMapCheckpointDto toShipmentMapCheckpointDto(
            ShipmentCheckpoint checkpoint,
            LogisticsNode node
    ) {
        return ShipmentMapCheckpointDto.builder()
                .nodePublicId(node != null ? node.getPublicId() : null)
                .nodeName(node != null ? node.getNodeName() : null)
                .nodeCode(node != null ? node.getNodeCode() : null)
                .checkpointType(checkpoint.getCheckpointType())
                .checkpointStatus(checkpoint.getCheckpointStatus())
                .plannedAt(checkpoint.getPlannedAt())
                .actualAt(checkpoint.getActualAt())
                .latitude(node != null ? node.getLatitude() : null)
                .longitude(node != null ? node.getLongitude() : null)
                .note(checkpoint.getNote())
                .build();
    }

    private ShipmentSourceType resolveSourceType(Shipment shipment) {
        return shipment.getSourceType() != null ? shipment.getSourceType() : ShipmentSourceType.ORDER;
    }
}

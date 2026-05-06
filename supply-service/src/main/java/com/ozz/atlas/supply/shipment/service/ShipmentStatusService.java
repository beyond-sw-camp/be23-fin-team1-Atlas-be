package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentLine;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatusHistory;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.settlement.service.SettlementService;
import com.ozz.atlas.supply.shipment.repository.ShipmentLineRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentStatusHistoryRepository;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class ShipmentStatusService {

    private final ShipmentRepository shipmentRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final ShipmentSearchService shipmentSearchService;
    private final ShipmentAuthorizationService shipmentAuthorizationService;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentLineRepository shipmentLineRepository;
    private final ShipmentInventoryService shipmentInventoryService;
    private final SettlementService settlementService;

    public ShipmentStatusService(
            ShipmentRepository shipmentRepository,
            LogisticsNodeRepository logisticsNodeRepository,
            ShipmentStatusHistoryRepository shipmentStatusHistoryRepository,
            ShipmentSearchService shipmentSearchService,
            ShipmentAuthorizationService shipmentAuthorizationService,
            ShipmentMapper shipmentMapper,
            ShipmentLineRepository shipmentLineRepository,
            ShipmentInventoryService shipmentInventoryService,
            SettlementService settlementService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.shipmentStatusHistoryRepository = shipmentStatusHistoryRepository;
        this.shipmentSearchService = shipmentSearchService;
        this.shipmentAuthorizationService = shipmentAuthorizationService;
        this.shipmentMapper = shipmentMapper;
        this.shipmentLineRepository = shipmentLineRepository;
        this.shipmentInventoryService = shipmentInventoryService;
        this.settlementService = settlementService;
    }

    public ShipmentResponseDto startShipment(
            String publicId,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);
        shipmentAuthorizationService.validateSenderCanStartShipment(shipment, organizationPublicId);

        if (shipment.getStatus() != ShipmentStatus.READY) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }

        LocalDateTime now = LocalDateTime.now();

        shipment.updateShipmentInfo(
                generateCarrierName(),
                generateVehicleNo(),
                generateTrackingNo(shipment),
                shipment.getOriginNodeId(),
                shipment.getDestinationNodeId(),
                shipment.getOriginNodeId(),
                shipment.getDepartureEta(),
                now.plusHours(24)
        );

        shipment.markInTransit(shipment.getOriginNodeId(), now);

        Shipment savedShipment = shipmentRepository.save(shipment);
        LogisticsNode originNode = getNode(savedShipment.getOriginNodeId());

        saveShipmentStatusHistory(
                savedShipment,
                now,
                "배송 중",
                originNode.getNodeName(),
                originNode.getLatitude(),
                originNode.getLongitude(),
                actorUserPublicId != null ? actorUserPublicId : "SYSTEM"
        );

        shipmentSearchService.saveShipmentDocument(savedShipment);

        return shipmentMapper.toShipmentResponseDto(savedShipment);
    }

    public ShipmentResponseDto arriveShipment(
            String publicId,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);
        shipmentAuthorizationService.validateReceiverCanArriveShipment(shipment, organizationPublicId);

        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT
                && shipment.getStatus() != ShipmentStatus.DELAYED) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }

        LocalDateTime now = LocalDateTime.now();

        shipment.markArrived(shipment.getDestinationNodeId(), now);

        Shipment savedShipment = shipmentRepository.save(shipment);
        LogisticsNode destinationNode = getNode(savedShipment.getDestinationNodeId());

        saveShipmentStatusHistory(
                savedShipment,
                now,
                "도착 완료",
                destinationNode.getNodeName(),
                destinationNode.getLatitude(),
                destinationNode.getLongitude(),
                actorUserPublicId != null ? actorUserPublicId : "SYSTEM"
        );

        shipmentSearchService.saveShipmentDocument(savedShipment);
        settlementService.createShipmentSettlementIfAbsent(savedShipment.getPublicId());

        return shipmentMapper.toShipmentResponseDto(savedShipment);
    }

    public ShipmentResponseDto cancelShipment(
            String publicId,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);
        shipmentAuthorizationService.validateSenderCanStartShipment(shipment, organizationPublicId);

        if (shipment.getStatus() != ShipmentStatus.READY) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }

        List<ShipmentLine> shipmentLines = shipmentLineRepository.findByShipmentIdOrderByIdAsc(shipment.getId());
        shipmentInventoryService.restoreDeductedForShipmentLines(shipmentLines);
        shipment.cancel();

        Shipment savedShipment = shipmentRepository.save(shipment);
        LogisticsNode originNode = getNode(savedShipment.getOriginNodeId());

        saveShipmentStatusHistory(
                savedShipment,
                LocalDateTime.now(),
                "출하 취소",
                originNode.getNodeName(),
                originNode.getLatitude(),
                originNode.getLongitude(),
                actorUserPublicId != null ? actorUserPublicId : "SYSTEM"
        );

        shipmentSearchService.saveShipmentDocument(savedShipment);

        return shipmentMapper.toShipmentResponseDto(savedShipment);
    }

    private Shipment getReadableShipment(String publicId, String organizationPublicId) {
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        LogisticsNode originNode = getNode(shipment.getOriginNodeId());
        LogisticsNode destinationNode = getNode(shipment.getDestinationNodeId());

        boolean readable =
                originNode.getOrganizationPublicId().equals(organizationPublicId)
                        || destinationNode.getOrganizationPublicId().equals(organizationPublicId);

        if (!readable) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND);
        }

        return shipment;
    }

    private LogisticsNode getNode(Long nodeId) {
        return logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
    }

    private void saveShipmentStatusHistory(
            Shipment shipment,
            LocalDateTime recordedAt,
            String statusMessage,
            String locationText,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude,
            String recordedBy
    ) {
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipmentId(shipment.getId())
                .statusCode(shipment.getStatus())
                .statusMessage(statusMessage)
                .locationText(locationText)
                .latitude(latitude)
                .longitude(longitude)
                .recordedAt(recordedAt != null ? recordedAt : LocalDateTime.now())
                .recordedBy(recordedBy)
                .build();

        shipmentStatusHistoryRepository.save(history);
    }

    private String generateCarrierName() {
        return "Atlas Express";
    }

    private String generateVehicleNo() {
        int number = (int) (Math.random() * 9000) + 1000;
        return "12가" + number;
    }

    private String generateTrackingNo(Shipment shipment) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "TRK-" + date + "-" + shipment.getPublicId().substring(0, 8);
    }
}

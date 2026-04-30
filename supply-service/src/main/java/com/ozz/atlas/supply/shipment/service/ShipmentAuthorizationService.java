package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import org.springframework.stereotype.Service;

@Service
public class ShipmentAuthorizationService {

    private static final String ADMIN_ORGANIZATION_TYPE = "ADMIN";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SUPPLIER_ORGANIZATION_TYPE = "SUPPLIER";

    private final LogisticsNodeRepository logisticsNodeRepository;

    public ShipmentAuthorizationService(LogisticsNodeRepository logisticsNodeRepository) {
        this.logisticsNodeRepository = logisticsNodeRepository;
    }

    public void validateShipmentActor(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (organizationPublicId == null || organizationPublicId.isBlank()
                || organizationType == null || organizationType.isBlank()) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        if (ADMIN_ORGANIZATION_TYPE.equalsIgnoreCase(organizationType)
                || ADMIN_ROLE.equalsIgnoreCase(userRole)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }

    public void validateCreateShipmentActor(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        if (!SUPPLIER_ORGANIZATION_TYPE.equalsIgnoreCase(organizationType)) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_CREATION_NOT_ALLOWED);
        }
    }

    public void validateReadableShipment(Shipment shipment, String organizationPublicId) {
        LogisticsNode originNode = getNode(shipment.getOriginNodeId());
        LogisticsNode destinationNode = getNode(shipment.getDestinationNodeId());

        boolean readable =
                originNode.getOrganizationPublicId().equals(organizationPublicId)
                        || destinationNode.getOrganizationPublicId().equals(organizationPublicId);

        if (!readable) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }

    public void validateSenderCanStartShipment(Shipment shipment, String organizationPublicId) {
        LogisticsNode originNode = getNode(shipment.getOriginNodeId());

        if (!originNode.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }

    public void validateReceiverCanArriveShipment(Shipment shipment, String organizationPublicId) {
        LogisticsNode destinationNode = getNode(shipment.getDestinationNodeId());

        if (!destinationNode.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }

    private LogisticsNode getNode(Long nodeId) {
        return logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
    }
}

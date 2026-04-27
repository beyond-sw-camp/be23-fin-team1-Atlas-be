package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.kafka.shipment.ShipmentFactory;
import com.ozz.atlas.supply.logistics.service.LogisticsNodeService;
import com.ozz.atlas.supply.shipment.domain.DeliveryException;
import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionType;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.dtos.CreateDeliveryExceptionRequestDto;
import com.ozz.atlas.supply.shipment.dtos.DeliveryExceptionResponseDto;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.DeliveryExceptionRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeliveryExceptionService {

    private final DeliveryExceptionRepository deliveryExceptionRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentSearchService shipmentSearchService;
    private final LogisticsNodeService logisticsNodeService;
    private final OutboxEventAppender outboxEventAppender;
    private final ShipmentFactory shipmentFactory;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;

    public DeliveryExceptionService(
            DeliveryExceptionRepository deliveryExceptionRepository,
            ShipmentRepository shipmentRepository,
            ShipmentSearchService shipmentSearchService,
            LogisticsNodeService logisticsNodeService,
            OutboxEventAppender outboxEventAppender,
            ShipmentFactory shipmentFactory,
            SupplyDomainEventFactory supplyDomainEventFactory,
            SupplyChainContextResolver supplyChainContextResolver
    ) {
        this.deliveryExceptionRepository = deliveryExceptionRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipmentSearchService = shipmentSearchService;
        this.logisticsNodeService = logisticsNodeService;
        this.outboxEventAppender = outboxEventAppender;
        this.shipmentFactory = shipmentFactory;
        this.supplyDomainEventFactory = supplyDomainEventFactory;
        this.supplyChainContextResolver = supplyChainContextResolver;
    }

    public DeliveryExceptionResponseDto createDeliveryException(
            CreateDeliveryExceptionRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        Shipment shipment = shipmentRepository.findByPublicId(dto.getShipmentPublicId())
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        DeliveryException deliveryException = DeliveryException.builder()
                .shipmentId(shipment.getId())
                .shipmentCheckpointId(null)
                .exceptionType(dto.getExceptionType())
                .severity(dto.getSeverity())
                .detectedAt(dto.getDetectedAt())
                .note(dto.getNote())
                .resolved(false)
                .build();

        DeliveryException savedException = deliveryExceptionRepository.save(deliveryException);
        SupplyChainContext context = supplyChainContextResolver.fromShipment(shipment);

        outboxEventAppender.append(
                supplyDomainEventFactory.create(
                        KafkaTopics.SUPPLY_DELIVERY_EXCEPTION,
                        resolveDeliveryExceptionEventType(dto.getExceptionType()),
                        AggregateType.DELIVERY_EXCEPTION,
                        savedException.getPublicId(),
                        actorUserPublicId,
                        organizationPublicId,
                        context,
                        supplyDomainEventFactory.payload(
                                savedException.getPublicId(),
                                shipment.getShipmentNumber(),
                                dto.getExceptionType().name(),
                                "배송 예외 발생",
                                dto.getExceptionType().name() + " 예외 발생 시",
                                null
                        )
                )
        );

        if (dto.getExceptionType() == DeliveryExceptionType.DELAY) {
            shipment.markDelayed();
            outboxEventAppender.append(
                    shipmentFactory.createShipmentDelayDetectedEvent(
                            shipment,
                            calculateDelayMinutes(shipment, dto.getDetectedAt()),
                            dto.getDetectedAt(),
                            getCurrentNodePublicId(shipment),
                            context,
                            actorUserPublicId,
                            organizationPublicId
                    )
            );
        }
        shipmentSearchService.saveShipmentDocument(shipment);


        return DeliveryExceptionResponseDto.from(savedException, shipment.getPublicId());
    }

    @Transactional(readOnly = true)
    public List<DeliveryExceptionResponseDto> getDeliveryExceptionsByShipmentPublicId(String shipmentPublicId) {
        Shipment shipment = shipmentRepository.findByPublicId(shipmentPublicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return deliveryExceptionRepository.findByShipmentIdOrderByDetectedAtDesc(shipment.getId())
                .stream()
                .map(deliveryException -> DeliveryExceptionResponseDto.from(deliveryException, shipment.getPublicId()))
                .toList();
    }

    private long calculateDelayMinutes(Shipment shipment, java.time.LocalDateTime detectedAt) {
        if (shipment.getArrivalEta() == null || detectedAt == null || !detectedAt.isAfter(shipment.getArrivalEta())) {
            return 0L;
        }
        return Duration.between(shipment.getArrivalEta(), detectedAt).toMinutes();
    }

    private String getCurrentNodePublicId(Shipment shipment) {
        if (shipment.getCurrentNodeId() == null) {
            return null;
        }
        return logisticsNodeService.getLogisticsNodeEntity(shipment.getCurrentNodeId()).getPublicId();
    }

    private String resolveDeliveryExceptionEventType(DeliveryExceptionType exceptionType) {
        if (exceptionType == DeliveryExceptionType.DELAY) {
            return EventTypes.DELIVERY_EXCEPTION_DELAY;
        }
        if (exceptionType == DeliveryExceptionType.TEMPERATURE_DEVIATION) {
            return EventTypes.DELIVERY_EXCEPTION_TEMPERATURE_DEVIATION;
        }
        if (exceptionType == DeliveryExceptionType.DAMAGE) {
            return EventTypes.DELIVERY_EXCEPTION_DAMAGED;
        }
        return EventTypes.DELIVERY_EXCEPTION_CREATED;
    }
}

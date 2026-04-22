package com.ozz.atlas.supply.kafka.shipment;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.EventSchemaVersions;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ShipmentFactory {

    private static final String PRODUCER = "supply-service";

    public EventEnvelope<ShipmentCreatedPayload> createShipmentCreatedEvent(
            Shipment shipment,
            String originNodePublicId,
            String destinationNodePublicId,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        // Kafka 계약에는 내부 PK 대신 publicId
        ShipmentCreatedPayload payload = new ShipmentCreatedPayload(
                shipment.getPublicId(),
                shipment.getShipmentNumber(),
                shipment.getPurchaseOrderPublicId(),
                shipment.getSubPurchaseOrderPublicId(),
                originNodePublicId,
                destinationNodePublicId,
                shipment.getDepartureEta(),
                shipment.getArrivalEta(),
                shipment.getStatus().name(),
                shipment.isTemperatureRequired()
        );

        return buildEnvelope(
                EventTypes.SHIPMENT_CREATED,
                shipment.getPublicId(),
                actorUserPublicId,
                organizationPublicId,
                payload
        );
    }

    public EventEnvelope<ShipmentDelayDetectedPayload> createShipmentDelayDetectedEvent(
            Shipment shipment,
            long delayMinutes,
            LocalDateTime estimatedArrivalAt,
            String currentNodePublicId,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        ShipmentDelayDetectedPayload payload = new ShipmentDelayDetectedPayload(
                shipment.getPublicId(),
                shipment.getShipmentNumber(),
                shipment.getStatus().name(),
                delayMinutes,
                shipment.getArrivalEta(),
                estimatedArrivalAt,
                currentNodePublicId
        );

        return buildEnvelope(
                EventTypes.SHIPMENT_DELAY_DETECTED,
                shipment.getPublicId(),
                actorUserPublicId,
                organizationPublicId,
                payload
        );
    }

    private <T> EventEnvelope<T> buildEnvelope(
            String eventType,
            String aggregatePublicId,
            String actorUserPublicId,
            String organizationPublicId,
            T payload
    ) {
        String eventId = PublicIdGenerator.next();

        return new EventEnvelope<>(
                eventId,
                eventType,
                EventSchemaVersions.V1,
                PRODUCER,
                KafkaTopics.SUPPLY_SHIPMENT,
                AggregateType.SHIPMENT,
                aggregatePublicId,
                // 같은 출하의 이벤트 순서를 한 파티션 안에서 유지하기 위해 aggregate publicId를 key로 쓴다.
                aggregatePublicId,
                Instant.now(),
                // 현재는 이벤트별 고유 흐름이므로 correlationId를 eventId와 동일하게 시작한다.
                eventId,
                null,
                actorUserPublicId,
                organizationPublicId,
                payload
        );
    }
}

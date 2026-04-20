package com.ozz.atlas.control.event.recommendation;

import com.ozz.atlas.common.event.AggregateType;
import com.ozz.atlas.common.event.EventEnvelope;
import com.ozz.atlas.common.event.EventSchemaVersions;
import com.ozz.atlas.common.event.EventTypes;
import com.ozz.atlas.common.event.KafkaTopics;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.control.event.shipment.ShipmentDelayDetectedPayload;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class RecommendationEventFactory {

    public EventEnvelope<RecommendationRequestedPayload> shipmentDelayRequested(
            EventEnvelope<?> sourceEvent,
            ShipmentDelayDetectedPayload payload
    ) {
        String recommendationPublicId = PublicIdGenerator.next();

        RecommendationRequestedPayload requestPayload = new RecommendationRequestedPayload(
                sourceEvent.eventId(),
                sourceEvent.eventType(),
                "shipment_delayed",
                payload.shipmentPublicId(),
                payload.shipmentNumber(),
                payload.delayMinutes(),
                payload.arrivalEta(),
                payload.estimatedArrivalAt(),
                payload.currentNodePublicId(),
                "출하 지연이 감지되어 대응 권고안 생성이 필요합니다."
        );

        return new EventEnvelope<>(
                PublicIdGenerator.next(),
                EventTypes.RECOMMENDATION_REQUESTED,
                EventSchemaVersions.V1,
                "control-service",
                KafkaTopics.CONTROL_RECOMMENDATION_REQUESTED,
                AggregateType.RECOMMENDATION,
                recommendationPublicId,
                sourceEvent.organizationPublicId() != null ? sourceEvent.organizationPublicId() : recommendationPublicId,
                Instant.now(),
                sourceEvent.correlationId() != null ? sourceEvent.correlationId() : sourceEvent.eventId(),
                sourceEvent.eventId(),
                sourceEvent.actorUserPublicId(),
                sourceEvent.organizationPublicId(),
                requestPayload
        );
    }
}

package com.ozz.atlas.supply.kafka.event;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.EventSchemaVersions;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SupplyDomainEventFactory {

    private static final String PRODUCER = "supply-service";

    public EventEnvelope<SupplyDomainEventPayload> create(
            String topic,
            String eventType,
            AggregateType aggregateType,
            String aggregatePublicId,
            String actorUserPublicId,
            String organizationPublicId,
            SupplyChainContext context,
            SupplyDomainEventPayload payload
    ) {
        String eventId = PublicIdGenerator.next();

        return new EventEnvelope<>(
                eventId,
                eventType,
                EventSchemaVersions.V1,
                PRODUCER,
                topic,
                aggregateType,
                aggregatePublicId,
                aggregatePublicId,
                Instant.now(),
                eventId,
                null,
                actorUserPublicId,
                organizationPublicId,
                withContext(payload, context)
        );
    }

    public SupplyDomainEventPayload payload(
            String referencePublicId,
            String referenceNumber,
            String status,
            String eventName,
            String description,
            String returnType
    ) {
        return new SupplyDomainEventPayload(
                referencePublicId,
                referenceNumber,
                status,
                eventName,
                description,
                returnType,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private SupplyDomainEventPayload withContext(SupplyDomainEventPayload payload, SupplyChainContext context) {
        SupplyChainContext safeContext = context != null ? context : SupplyChainContext.empty();
        return new SupplyDomainEventPayload(
                payload.referencePublicId(),
                payload.referenceNumber(),
                payload.status(),
                payload.eventName(),
                payload.description(),
                payload.returnType(),
                safeContext.rootPurchaseOrderPublicId(),
                safeContext.rootBuyerOrganizationPublicId(),
                safeContext.directBuyerOrganizationPublicId(),
                safeContext.directSupplierOrganizationPublicId(),
                safeContext.parentPurchaseOrderPublicId(),
                safeContext.subPurchaseOrderPublicId()
        );
    }
}

from datetime import datetime, timezone
import secrets
from typing import Any

from pydantic import BaseModel, Field

from app.messaging import topics
from app.schemas.recommendation_item import RecommendationItem
from app.schemas.recommendation_request import RecommendationRequest
from app.schemas.recommendation_result import RecommendationResult


class RecommendationRequestedPayload(BaseModel):
    source_event_id: str = Field(alias="sourceEventId")
    source_event_type: str = Field(alias="sourceEventType")
    risk_type: str = Field(alias="riskType")
    shipment_public_id: str = Field(alias="shipmentPublicId")
    shipment_number: str = Field(alias="shipmentNumber")
    delay_minutes: int = Field(alias="delayMinutes")
    arrival_eta: datetime | None = Field(default=None, alias="arrivalEta")
    estimated_arrival_at: datetime | None = Field(default=None, alias="estimatedArrivalAt")
    current_node_public_id: str | None = Field(default=None, alias="currentNodePublicId")
    summary: str

    def to_recommendation_request(self) -> RecommendationRequest:
        severity = "HIGH" if self.delay_minutes >= 180 else "MEDIUM" if self.delay_minutes >= 60 else "LOW"
        facts = [
            f"shipmentPublicId={self.shipment_public_id}",
            f"shipmentNumber={self.shipment_number}",
            f"delayMinutes={self.delay_minutes}",
        ]

        if self.arrival_eta is not None:
            facts.append(f"arrivalEta={self.arrival_eta.isoformat()}")
        if self.estimated_arrival_at is not None:
            facts.append(f"estimatedArrivalAt={self.estimated_arrival_at.isoformat()}")
        if self.current_node_public_id:
            facts.append(f"currentNodePublicId={self.current_node_public_id}")

        return RecommendationRequest(
            risk_public_id=self.shipment_public_id,
            risk_type=self.risk_type,
            severity=severity,
            facts=facts,
            requested_count=3,
            selected_model_key="KR_MAIN",
        )


class EventEnvelope(BaseModel):
    event_id: str = Field(alias="eventId")
    event_type: str = Field(alias="eventType")
    schema_version: str = Field(alias="schemaVersion")
    producer: str
    topic: str
    aggregate_type: str = Field(alias="aggregateType")
    aggregate_public_id: str = Field(alias="aggregatePublicId")
    partition_key: str = Field(alias="partitionKey")
    occurred_at: datetime = Field(alias="occurredAt")
    correlation_id: str | None = Field(default=None, alias="correlationId")
    causation_id: str | None = Field(default=None, alias="causationId")
    actor_user_public_id: str | None = Field(default=None, alias="actorUserPublicId")
    organization_public_id: str | None = Field(default=None, alias="organizationPublicId")
    payload: dict[str, Any]

    def request_payload(self) -> RecommendationRequestedPayload:
        return RecommendationRequestedPayload.model_validate(self.payload)


class RecommendationGeneratedPayload(BaseModel):
    source_event_id: str = Field(alias="sourceEventId")
    source_event_type: str = Field(alias="sourceEventType")
    recommendation_public_id: str = Field(alias="recommendationPublicId")
    shipment_public_id: str = Field(alias="shipmentPublicId")
    risk_type: str = Field(alias="riskType")
    provider: str
    model: str
    model_version: str | None = Field(default=None, alias="modelVersion")
    recommendations: list[RecommendationItem]


class RecommendationFailedPayload(BaseModel):
    source_event_id: str = Field(alias="sourceEventId")
    source_event_type: str = Field(alias="sourceEventType")
    recommendation_public_id: str = Field(alias="recommendationPublicId")
    shipment_public_id: str = Field(alias="shipmentPublicId")
    risk_type: str = Field(alias="riskType")
    error_message: str = Field(alias="errorMessage")


def build_generated_event(
    source_event: EventEnvelope,
    request_payload: RecommendationRequestedPayload,
    result: RecommendationResult,
) -> dict[str, Any]:
    occurred_at = datetime.now(timezone.utc).isoformat()
    payload = RecommendationGeneratedPayload(
        sourceEventId=source_event.event_id,
        sourceEventType=source_event.event_type,
        recommendationPublicId=source_event.aggregate_public_id,
        shipmentPublicId=request_payload.shipment_public_id,
        riskType=request_payload.risk_type,
        provider=result.provider,
        model=result.model,
        modelVersion=result.model_version,
        recommendations=result.recommendations,
    )

    return {
        "eventId": secrets.token_hex(13),
        "eventType": topics.EVENT_TYPE_RECOMMENDATION_GENERATED,
        "schemaVersion": "v1",
        "producer": "ai-orchestrator-service",
        "topic": topics.RECOMMENDATION_GENERATED,
        "aggregateType": "RECOMMENDATION",
        "aggregatePublicId": source_event.aggregate_public_id,
        "partitionKey": source_event.partition_key,
        "occurredAt": occurred_at,
        "correlationId": source_event.correlation_id or source_event.event_id,
        "causationId": source_event.event_id,
        "actorUserPublicId": source_event.actor_user_public_id,
        "organizationPublicId": source_event.organization_public_id,
        "payload": payload.model_dump(by_alias=True),
    }


def build_failed_event(
    source_event: EventEnvelope,
    request_payload: RecommendationRequestedPayload,
    error_message: str,
) -> dict[str, Any]:
    occurred_at = datetime.now(timezone.utc).isoformat()
    payload = RecommendationFailedPayload(
        sourceEventId=source_event.event_id,
        sourceEventType=source_event.event_type,
        recommendationPublicId=source_event.aggregate_public_id,
        shipmentPublicId=request_payload.shipment_public_id,
        riskType=request_payload.risk_type,
        errorMessage=error_message,
    )

    return {
        "eventId": secrets.token_hex(13),
        "eventType": topics.EVENT_TYPE_RECOMMENDATION_FAILED,
        "schemaVersion": "v1",
        "producer": "ai-orchestrator-service",
        "topic": topics.RECOMMENDATION_FAILED,
        "aggregateType": "RECOMMENDATION",
        "aggregatePublicId": source_event.aggregate_public_id,
        "partitionKey": source_event.partition_key,
        "occurredAt": occurred_at,
        "correlationId": source_event.correlation_id or source_event.event_id,
        "causationId": source_event.event_id,
        "actorUserPublicId": source_event.actor_user_public_id,
        "organizationPublicId": source_event.organization_public_id,
        "payload": payload.model_dump(by_alias=True),
    }

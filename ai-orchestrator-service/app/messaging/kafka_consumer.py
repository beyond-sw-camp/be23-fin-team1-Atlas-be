import asyncio
import json
import logging

from aiokafka import AIOKafkaConsumer

from app.config import settings
from app.messaging.contracts import (
    EventEnvelope,
    build_failed_event,
    build_generated_event,
)
from app.messaging.kafka_producer import KafkaProducerClient
from app.services.recommendation_service import RecommendationService

logger = logging.getLogger(__name__)


class KafkaConsumerRunner:
    def __init__(
        self,
        recommendation_service: RecommendationService,
        producer_client: KafkaProducerClient,
    ) -> None:
        self._recommendation_service = recommendation_service
        self._producer_client = producer_client
        self._consumer = AIOKafkaConsumer(
            settings.recommendation_request_topic,
            bootstrap_servers=settings.kafka_bootstrap_servers,
            security_protocol=settings.kafka_security_protocol,
            sasl_mechanism=settings.kafka_sasl_mechanism,
            sasl_plain_username=settings.kafka_username,
            sasl_plain_password=settings.kafka_password,
            client_id=f"{settings.kafka_client_id}-consumer",
            group_id=settings.kafka_consumer_group_id,
            enable_auto_commit=False,
            auto_offset_reset="earliest",
            value_deserializer=lambda value: json.loads(value.decode("utf-8")),
        )
        self._task = None
        self._running = False

    async def start(self) -> None:
        await self._consumer.start()
        self._running = True
        self._task = asyncio.create_task(self._consume_loop())

    async def stop(self) -> None:
        self._running = False
        if self._task:
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass
        await self._consumer.stop()

    async def _consume_loop(self) -> None:
        async for message in self._consumer:
            if not self._running:
                break

            envelope = EventEnvelope.model_validate(message.value)
            request_payload = envelope.request_payload()

            try:
                result = await self._recommendation_service.generate(
                    request_payload.to_recommendation_request()
                )
                result_event = build_generated_event(envelope, request_payload, result)
                await self._producer_client.publish(
                    settings.recommendation_result_topic,
                    result_event,
                    key=envelope.partition_key,
                )
                await self._consumer.commit()
            except Exception as exc:
                logger.exception("권고안 생성 이벤트 처리에 실패했습니다.")
                failed_event = build_failed_event(envelope, request_payload, str(exc))
                await self._producer_client.publish(
                    settings.recommendation_failed_topic,
                    failed_event,
                    key=envelope.partition_key,
                )
                await self._consumer.commit()

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
        # ai-orchestrator는 생성 성공/실패 이벤트를 직접 발행한 뒤에만 offset을 커밋한다.
        self._task = None
        self._running = False

    async def start(self) -> None:
        logger.info(
            "Kafka consumer connecting. bootstrap_servers=%s topic=%s group_id=%s",
            settings.kafka_bootstrap_servers,
            settings.recommendation_request_topic,
            settings.kafka_consumer_group_id,
        )
        await self._consumer.start()
        self._running = True
        self._task = asyncio.create_task(self._consume_loop())
        logger.info("Kafka consumer connected")

    async def stop(self) -> None:
        self._running = False
        if self._task:
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass
        await self._consumer.stop()
        logger.info("Kafka consumer stopped")

    async def _consume_loop(self) -> None:
        async for message in self._consumer:
            if not self._running:
                break

            logger.info(
                "Kafka message received. topic=%s partition=%s offset=%s",
                message.topic,
                message.partition,
                message.offset,
            )

            envelope = None
            request_payload = None

            try:
                envelope = EventEnvelope.model_validate(message.value)
                request_payload = envelope.request_payload()
                result = await self._recommendation_service.generate(
                    request_payload.to_recommendation_request()
                )
                # 생성 성공 시 generated 이벤트를 먼저 발행하고 offset을 커밋한다.
                result_event = build_generated_event(envelope, request_payload, result)
                await self._producer_client.publish(
                    settings.recommendation_result_topic,
                    result_event,
                    key=envelope.partition_key,
                )
                await self._consumer.commit()
            except Exception as exc:
                logger.exception("Kafka 메시지 파싱 또는 권고안 생성 처리에 실패했습니다.")
                if envelope is None or request_payload is None:
                    continue
                # 생성 실패도 failed 이벤트로 남겨 control-service가 후속 처리할 수 있게 한다.
                failed_event = build_failed_event(envelope, request_payload, str(exc))
                await self._producer_client.publish(
                    settings.recommendation_failed_topic,
                    failed_event,
                    key=envelope.partition_key,
                )
                await self._consumer.commit()

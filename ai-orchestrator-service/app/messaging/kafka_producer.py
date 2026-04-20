import json

from aiokafka import AIOKafkaProducer

from app.config import settings


class KafkaProducerClient:
    def __init__(self) -> None:
        # Java 서비스와 같은 SASL/PLAIN 설정으로 동일한 클러스터에 접속한다.
        self._producer = AIOKafkaProducer(
            bootstrap_servers=settings.kafka_bootstrap_servers,
            security_protocol=settings.kafka_security_protocol,
            sasl_mechanism=settings.kafka_sasl_mechanism,
            sasl_plain_username=settings.kafka_username,
            sasl_plain_password=settings.kafka_password,
            client_id=settings.kafka_client_id,
            value_serializer=lambda value: json.dumps(value).encode("utf-8"),
        )

    async def start(self) -> None:
        await self._producer.start()

    async def stop(self) -> None:
        await self._producer.stop()

    async def publish(self, topic: str, payload: dict, key: str | None = None) -> None:
        encoded_key = key.encode("utf-8") if key else None
        await self._producer.send_and_wait(topic, payload, key=encoded_key)

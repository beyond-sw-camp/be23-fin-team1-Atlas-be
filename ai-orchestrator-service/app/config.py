from dataclasses import dataclass
import os


@dataclass(frozen=True)
class Settings:
    kafka_bootstrap_servers: str = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    kafka_security_protocol: str = os.getenv("KAFKA_SECURITY_PROTOCOL", "SASL_PLAINTEXT")
    kafka_sasl_mechanism: str = os.getenv("KAFKA_SASL_MECHANISM", "PLAIN")
    kafka_username: str = os.getenv("KAFKA_USERNAME", "atlas")
    kafka_password: str = os.getenv("KAFKA_PASSWORD", "atlas")
    kafka_consumer_group_id: str = os.getenv("KAFKA_CONSUMER_GROUP_ID", "atlas-ai-recommendation")
    kafka_client_id: str = os.getenv("KAFKA_CLIENT_ID", "ai-orchestrator-service")

    recommendation_request_topic: str = os.getenv(
        "KAFKA_RECOMMENDATION_REQUEST_TOPIC",
        "atlas.control.recommendation-requested"
    )
    recommendation_result_topic: str = os.getenv(
        "KAFKA_RECOMMENDATION_RESULT_TOPIC",
        "atlas.control.recommendation-generated"
    )
    recommendation_failed_topic: str = os.getenv(
        "KAFKA_RECOMMENDATION_FAILED_TOPIC",
        "atlas.control.recommendation-failed"
    )

    local_llm_base_url: str = os.getenv("LOCAL_LLM_BASE_URL", "http://127.0.0.1:1234")
    local_llm_default_model: str = os.getenv(
        "LOCAL_LLM_DEFAULT_MODEL",
        "supergemma4-e4b-abliterated-mlx",
    )
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")
    openai_base_url: str | None = os.getenv("OPENAI_BASE_URL")
    request_timeout_seconds: int = int(os.getenv("REQUEST_TIMEOUT_SECONDS", "30"))


settings = Settings()

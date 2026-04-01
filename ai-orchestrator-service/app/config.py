from dataclasses import dataclass
import os


@dataclass(frozen=True)
class Settings:
    kafka_bootstrap_servers: str = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    recommendation_request_topic: str = os.getenv("KAFKA_RECOMMENDATION_REQUEST_TOPIC", "recommendation.requested.v1")
    recommendation_result_topic: str = os.getenv("KAFKA_RECOMMENDATION_RESULT_TOPIC", "recommendation.generated.v1")
    recommendation_failed_topic: str = os.getenv("KAFKA_RECOMMENDATION_FAILED_TOPIC", "recommendation.failed.v1")

    local_llm_base_url: str = os.getenv("LOCAL_LLM_BASE_URL", "http://localhost:8001")
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")
    openai_base_url: str | None = os.getenv("OPENAI_BASE_URL")
    request_timeout_seconds: int = int(os.getenv("REQUEST_TIMEOUT_SECONDS", "30"))


settings = Settings()

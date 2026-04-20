from fastapi import FastAPI

from app.api.health import router as health_router
from app.handlers.recommendation_handler import RecommendationHandler
from app.messaging.kafka_consumer import KafkaConsumerRunner
from app.messaging.kafka_producer import KafkaProducerClient
from app.providers.local_llm_provider import LocalLlmProvider
from app.providers.openai_provider import OpenAIProvider
from app.providers.router import ProviderRouter
from app.schemas.recommendation_request import RecommendationRequest
from app.schemas.recommendation_result import RecommendationResult
from app.services.output_parser import OutputParser
from app.services.prompt_builder import PromptBuilder
from app.services.recommendation_service import RecommendationService

app = FastAPI(title="AI Orchestrator Service")
app.include_router(health_router)

provider_router = ProviderRouter()
prompt_builder = PromptBuilder()
output_parser = OutputParser()
local_provider = LocalLlmProvider()
openai_provider = OpenAIProvider()
recommendation_service = RecommendationService(
    provider_router=provider_router,
    prompt_builder=prompt_builder,
    output_parser=output_parser,
    local_provider=local_provider,
    openai_provider=openai_provider,
)
recommendation_handler = RecommendationHandler(recommendation_service)
producer_client = KafkaProducerClient()
consumer_runner = KafkaConsumerRunner(
    recommendation_service=recommendation_service,
    producer_client=producer_client,
)


@app.on_event("startup")
async def startup() -> None:
    await producer_client.start()
    await consumer_runner.start()


@app.on_event("shutdown")
async def shutdown() -> None:
    await consumer_runner.stop()
    await producer_client.stop()


@app.post("/internal/recommendations/generate", response_model=RecommendationResult)
async def generate_recommendations(request: RecommendationRequest) -> RecommendationResult:
    return await recommendation_handler.handle(request.model_dump())

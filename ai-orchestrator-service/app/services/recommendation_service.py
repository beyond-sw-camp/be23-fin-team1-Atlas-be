from app.providers.local_llm_provider import LocalLlmProvider
from app.providers.openai_provider import OpenAIProvider
from app.providers.router import ProviderRouter
from app.schemas.recommendation_request import RecommendationRequest
from app.schemas.recommendation_result import RecommendationResult
from app.services.output_parser import OutputParser
from app.services.prompt_builder import PromptBuilder


class RecommendationService:
    def __init__(
            self,
            provider_router: ProviderRouter,
            prompt_builder: PromptBuilder,
            output_parser: OutputParser,
            local_provider: LocalLlmProvider,
            openai_provider: OpenAIProvider,
    ) -> None:
        self.provider_router = provider_router
        self.prompt_builder = prompt_builder
        self.output_parser = output_parser
        self.providers = {
            "LOCAL": local_provider,
            "OPENAI": openai_provider,
        }

    async def generate(self, request: RecommendationRequest) -> RecommendationResult:
        model_config = self.provider_router.resolve(request.selected_model_key)
        prompt = self.prompt_builder.build(request)
        provider = self.providers[model_config.provider]

        raw_output = await provider.generate(model_config.model, prompt)
        recommendations = self.output_parser.parse(raw_output)

        if len(recommendations) != request.requested_count:
            raise ValueError(
                f"Expected {request.requested_count} recommendations, got {len(recommendations)}."
            )

        return RecommendationResult(
            provider=model_config.provider,
            model=model_config.model,
            model_version=model_config.version,
            recommendations=recommendations,
        )

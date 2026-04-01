from app.schemas.recommendation_request import RecommendationRequest
from app.schemas.recommendation_result import RecommendationResult
from app.services.recommendation_service import RecommendationService


class RecommendationHandler:
    def __init__(self, recommendation_service: RecommendationService) -> None:
        self.recommendation_service = recommendation_service

    async def handle(self, payload: dict) -> RecommendationResult:
        request = RecommendationRequest.model_validate(payload)
        return await self.recommendation_service.generate(request)

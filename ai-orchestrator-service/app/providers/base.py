from typing import Protocol

from app.schemas.recommendation_request import RecommendationRequest
from app.schemas.recommendation_result import RecommendationResult


class RecommendationProvider(Protocol):
    async def generate(self, request: RecommendationRequest, prompt: str) -> RecommendationResult:
        ...

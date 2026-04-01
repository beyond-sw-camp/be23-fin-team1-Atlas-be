from pydantic import BaseModel

from app.schemas.recommendation_item import RecommendationItem


class RecommendationResult(BaseModel):
    provider: str
    model: str
    model_version: str | None = None
    recommendations: list[RecommendationItem]

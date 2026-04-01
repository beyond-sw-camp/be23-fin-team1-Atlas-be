from pydantic import BaseModel, Field


class RecommendationItem(BaseModel):
    title: str
    reason: str
    action: str
    priority: int = Field(ge=1, le=5)
    confidence: float = Field(ge=0.0, le=1.0)

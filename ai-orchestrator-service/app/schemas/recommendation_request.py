from pydantic import BaseModel, Field


class RecommendationRequest(BaseModel):
    risk_public_id: str
    risk_type: str
    severity: str
    facts: list[str] = Field(default_factory=list)
    requested_count: int = Field(default=3, ge=3, le=5)
    selected_model_key: str = "KR_MAIN"

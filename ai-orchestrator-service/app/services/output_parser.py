import json
import re

from app.schemas.recommendation_item import RecommendationItem


class OutputParser:
    def parse(self, raw_output: str) -> list[RecommendationItem]:
        normalized = raw_output.strip()
        normalized = re.sub(r"^```json\s*", "", normalized)
        normalized = re.sub(r"^```", "", normalized)
        normalized = re.sub(r"\s*```$", "", normalized)

        payload = json.loads(normalized)
        recommendations = payload.get("recommendations", [])

        if not isinstance(recommendations, list):
            raise ValueError("recommendations must be a list.")

        return [RecommendationItem.model_validate(item) for item in recommendations]

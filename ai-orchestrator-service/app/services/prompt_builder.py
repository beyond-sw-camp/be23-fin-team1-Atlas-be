import json

from app.schemas.recommendation_request import RecommendationRequest


class PromptBuilder:
    def build(self, request: RecommendationRequest) -> str:
        facts_json = json.dumps(request.facts, ensure_ascii=False)

        return f"""
You are a supply chain risk recommendation assistant.

Generate exactly {request.requested_count} recommendations.
Return ONLY valid JSON.
Do not use markdown fences.
Write the output in Korean unless the input is clearly English-only.

Return this exact schema:
{{
  "recommendations": [
    {{
      "title": "짧은 권고안 제목",
      "reason": "왜 이 권고가 필요한지",
      "action": "실행 방안",
      "priority": 1,
      "confidence": 0.0
    }}
  ]
}}

Rules:
- recommendations length must be exactly {request.requested_count}
- priority must be an integer from 1 to 5
- confidence must be a float from 0.0 to 1.0
- avoid duplicated recommendations
- reason and action must be concrete and operational

Risk input:
- risk_public_id: {request.risk_public_id}
- risk_type: {request.risk_type}
- severity: {request.severity}
- facts: {facts_json}
""".strip()

from openai import AsyncOpenAI

from app.config import settings


class OpenAIProvider:
    def __init__(self) -> None:
        self.client = AsyncOpenAI(
            api_key=settings.openai_api_key,
            base_url=settings.openai_base_url or None,
        )

    async def generate(self, model_name: str, prompt: str) -> str:
        if not settings.openai_api_key:
            raise ValueError("OPENAI_API_KEY is not configured.")

        response = await self.client.responses.create(
            model=model_name,
            instructions="You are a supply chain risk recommendation assistant. Return JSON only.",
            input=prompt,
        )

        return response.output_text

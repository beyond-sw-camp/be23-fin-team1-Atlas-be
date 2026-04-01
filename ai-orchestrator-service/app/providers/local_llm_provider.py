import httpx

from app.config import settings


class LocalLlmProvider:
    async def generate(self, model_name: str, prompt: str) -> str:
        payload = {
            "model": model_name,
            "messages": [
                {
                    "role": "system",
                    "content": "You are a supply chain risk recommendation assistant. Return JSON only."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            "temperature": 0.2
        }

        async with httpx.AsyncClient(timeout=settings.request_timeout_seconds) as client:
            response = await client.post(
                f"{settings.local_llm_base_url}/v1/chat/completions",
                json=payload,
            )
            response.raise_for_status()
            data = response.json()

        return data["choices"][0]["message"]["content"]

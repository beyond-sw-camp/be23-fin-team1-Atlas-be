from dataclasses import dataclass

from app.config import settings


@dataclass(frozen=True)
class ModelConfig:
    provider: str
    model: str
    version: str | None = None


MODEL_REGISTRY = {
    # 현재 로컬 개발 기본값은 LM Studio에 올라간 단일 모델을 공통으로 사용한다.
    "KR_MAIN": ModelConfig(provider="LOCAL", model=settings.local_llm_default_model),
    "KR_LIGHT": ModelConfig(provider="LOCAL", model=settings.local_llm_default_model),
    "EN_MAIN": ModelConfig(provider="LOCAL", model=settings.local_llm_default_model),
    "EN_LIGHT": ModelConfig(provider="LOCAL", model=settings.local_llm_default_model),
    "GPT": ModelConfig(provider="OPENAI", model="gpt-5.4-mini"),
}


class ProviderRouter:
    def resolve(self, selected_model_key: str | None) -> ModelConfig:
        if not selected_model_key:
            return MODEL_REGISTRY["KR_MAIN"]
        return MODEL_REGISTRY.get(selected_model_key, MODEL_REGISTRY["KR_MAIN"])

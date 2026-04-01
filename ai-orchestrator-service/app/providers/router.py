from dataclasses import dataclass


@dataclass(frozen=True)
class ModelConfig:
    provider: str
    model: str
    version: str | None = None


MODEL_REGISTRY = {
    "KR_MAIN": ModelConfig(provider="LOCAL", model="exaone-3.5-7.8b-instruct"),
    "KR_LIGHT": ModelConfig(provider="LOCAL", model="koni-7b-r"),
    "EN_MAIN": ModelConfig(provider="LOCAL", model="qwen3-8b"),
    "EN_LIGHT": ModelConfig(provider="LOCAL", model="bonsai-8b"),
    "GPT": ModelConfig(provider="OPENAI", model="gpt-5.4-mini"),
}


class ProviderRouter:
    def resolve(self, selected_model_key: str | None) -> ModelConfig:
        if not selected_model_key:
            return MODEL_REGISTRY["KR_MAIN"]
        return MODEL_REGISTRY.get(selected_model_key, MODEL_REGISTRY["KR_MAIN"])

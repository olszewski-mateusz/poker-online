from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )

    app_host: str = Field(default="0.0.0.0", alias="APP_HOST")
    app_port: int = Field(default=8081, alias="APP_PORT")
    poker_server_url: str = Field(default="http://localhost:8080", alias="POKER_SERVER_URL")
    request_timeout_seconds: float = Field(default=3.0, alias="REQUEST_TIMEOUT_SECONDS")
    ai_action_delay_min_seconds: float = Field(default=2.0, alias="AI_ACTION_DELAY_MIN_SECONDS")
    ai_action_delay_max_seconds: float = Field(default=3.0, alias="AI_ACTION_DELAY_MAX_SECONDS")
    ai_decision_seed: int | None = Field(default=None, alias="AI_DECISION_SEED")
    log_level: str = Field(default="INFO", alias="LOG_LEVEL")


@lru_cache
def get_settings() -> Settings:
    return Settings()

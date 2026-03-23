from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field

Aggressivity = Literal["LOW", "MEDIUM", "HIGH"]
ActionType = Literal[
    "ALL_IN",
    "BET",
    "CALL",
    "CHECK",
    "FOLD",
    "JOIN",
    "AI_JOIN",
    "RAISE",
    "READY",
    "REPLACE",
]


class AiConfiguration(BaseModel):
    model_config = ConfigDict(extra="ignore")

    aggressivity: Aggressivity


class ValidAction(BaseModel):
    model_config = ConfigDict(extra="ignore")

    type: ActionType
    properties: dict[str, str] = Field(default_factory=dict)


class GameResponse(BaseModel):
    model_config = ConfigDict(extra="allow")

    gameId: str
    myId: str
    validActions: list[ValidAction] = Field(default_factory=list)
    phase: str | None = None
    currentPlayerIndex: int | None = None
    players: list[dict[str, Any]] | None = None
    history: list[dict[str, Any]] | None = None
    configuration: dict[str, Any] | None = None


class AiRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    aiConfiguration: AiConfiguration
    gameResponse: GameResponse

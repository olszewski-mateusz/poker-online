from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True, slots=True)
class PlannedAction:
    game_id: str
    action_type: str
    endpoint: str
    payload: dict[str, Any]

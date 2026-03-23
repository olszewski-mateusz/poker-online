from dataclasses import dataclass
from typing import Any

from app.models.ai_request import AiRequest, GameResponse


@dataclass(frozen=True, slots=True)
class PlannedAction:
    game_id: str
    action_type: str
    endpoint: str
    payload: dict[str, Any]


class DecisionService:
    _PRIORITY = ("READY", "CHECK", "CALL", "BET", "RAISE", "ALL_IN", "REPLACE", "FOLD")

    def choose_action(self, ai_request: AiRequest) -> PlannedAction | None:
        game_response = ai_request.gameResponse
        actions_by_type = {action.type: action for action in game_response.validActions}

        for action_type in self._PRIORITY:
            valid_action = actions_by_type.get(action_type)
            if valid_action is None:
                continue

            planned_action = self._build_planned_action(
                game_response=game_response,
                action_type=action_type,
                properties=valid_action.properties,
            )
            if planned_action is not None:
                return planned_action

        return None

    def _build_planned_action(
        self,
        game_response: GameResponse,
        action_type: str,
        properties: dict[str, str],
    ) -> PlannedAction | None:
        game_id = game_response.gameId
        player_id = game_response.myId

        if action_type == "READY":
            if properties.get("ready", "").lower() != "true":
                return None
            return PlannedAction(
                game_id=game_id,
                action_type=action_type,
                endpoint="ready",
                payload={"playerId": player_id, "ready": True},
            )

        if action_type in {"CHECK", "CALL", "ALL_IN", "FOLD"}:
            endpoint = {
                "CHECK": "check",
                "CALL": "call",
                "ALL_IN": "all-in",
                "FOLD": "fold",
            }[action_type]
            return PlannedAction(
                game_id=game_id,
                action_type=action_type,
                endpoint=endpoint,
                payload={"playerId": player_id},
            )

        if action_type in {"BET", "RAISE"}:
            min_amount = self._parse_int(properties.get("min"))
            if min_amount is None:
                return None
            endpoint = "bet" if action_type == "BET" else "raise"
            return PlannedAction(
                game_id=game_id,
                action_type=action_type,
                endpoint=endpoint,
                payload={"playerId": player_id, "amount": min_amount},
            )

        if action_type == "REPLACE":
            return PlannedAction(
                game_id=game_id,
                action_type=action_type,
                endpoint="replace",
                payload={"playerId": player_id, "cardsToReplace": []},
            )

        return None

    @staticmethod
    def _parse_int(value: str | None) -> int | None:
        if value is None:
            return None

        try:
            return int(value)
        except ValueError:
            return None

from app.models.ai_request import AiRequest
from app.services.decision_service import DecisionService


def build_request(valid_actions: list[dict]) -> AiRequest:
    return AiRequest.model_validate(
        {
            "aiConfiguration": {"aggressivity": "MEDIUM"},
            "gameResponse": {
                "gameId": "game-1",
                "myId": "player-1",
                "validActions": valid_actions,
            },
        }
    )


def test_priority_prefers_check_over_fold() -> None:
    service = DecisionService()
    ai_request = build_request(
        [
            {"type": "FOLD", "properties": {}},
            {"type": "CHECK", "properties": {}},
        ]
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "CHECK"
    assert result.endpoint == "check"
    assert result.payload == {"playerId": "player-1"}


def test_bet_uses_min_amount() -> None:
    service = DecisionService()
    ai_request = build_request(
        [
            {"type": "BET", "properties": {"min": "37", "max": "500"}},
        ]
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "BET"
    assert result.endpoint == "bet"
    assert result.payload == {"playerId": "player-1", "amount": 37}


def test_ready_only_when_true() -> None:
    service = DecisionService()
    ai_request = build_request(
        [
            {"type": "READY", "properties": {"ready": "false"}},
            {"type": "FOLD", "properties": {}},
        ]
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "FOLD"
    assert result.endpoint == "fold"


def test_returns_none_when_no_action_is_valid() -> None:
    service = DecisionService()
    ai_request = build_request([])

    result = service.choose_action(ai_request)

    assert result is None

from random import Random

from app.models.ai_request import AiRequest
from app.services.decision_service import DecisionService


def build_request(
    *,
    valid_actions: list[dict],
    phase: str = "FIRST_BETTING",
    aggressivity: str = "MEDIUM",
    hand_type: str = "HIGH_CARD",
    cards: list[dict] | None = None,
    my_chips: int = 200,
    my_bet: int = 0,
    opponent_bet: int = 0,
    pot_chips: int = 60,
) -> AiRequest:
    my_cards = cards or [
        {"rank": "TWO", "suit": "CLUBS"},
        {"rank": "SEVEN", "suit": "DIAMONDS"},
        {"rank": "NINE", "suit": "SPADES"},
        {"rank": "JACK", "suit": "HEARTS"},
        {"rank": "QUEEN", "suit": "CLUBS"},
    ]

    return AiRequest.model_validate(
        {
            "aiConfiguration": {"aggressivity": aggressivity},
            "gameResponse": {
                "gameId": "game-1",
                "myId": "player-1",
                "myIndex": 0,
                "phase": phase,
                "pot": {
                    "chips": pot_chips,
                    "mainPot": {"chips": pot_chips, "winnerIndexes": []},
                    "sidePots": [],
                },
                "configuration": {
                    "minPlayersToStart": 2,
                    "startChips": 300,
                    "ante": 5,
                    "minOpenBet": 10,
                },
                "players": [
                    {
                        "index": 0,
                        "name": "AI",
                        "chips": my_chips,
                        "bet": my_bet,
                        "ready": False,
                        "folded": False,
                        "cards": my_cards,
                        "handType": hand_type,
                    },
                    {
                        "index": 1,
                        "name": "Opponent",
                        "chips": 250,
                        "bet": opponent_bet,
                        "ready": False,
                        "folded": False,
                        "cards": None,
                        "handType": None,
                    },
                ],
                "validActions": valid_actions,
            },
        }
    )


def test_ready_action_when_ready_true() -> None:
    service = DecisionService(random_source=Random(2))
    ai_request = build_request(
        phase="NOT_STARTED",
        valid_actions=[{"type": "READY", "properties": {"ready": "true"}}],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "READY"
    assert result.endpoint == "ready"
    assert result.payload == {"playerId": "player-1", "ready": True}


def test_replace_action_selected_in_drawing_phase() -> None:
    service = DecisionService(random_source=Random(11))
    ai_request = build_request(
        phase="DRAWING",
        hand_type="PAIR",
        cards=[
            {"rank": "ACE", "suit": "HEARTS"},
            {"rank": "ACE", "suit": "SPADES"},
            {"rank": "THREE", "suit": "CLUBS"},
            {"rank": "SEVEN", "suit": "DIAMONDS"},
            {"rank": "NINE", "suit": "HEARTS"},
        ],
        valid_actions=[{"type": "REPLACE", "properties": {"min": "0", "max": "5"}}],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "REPLACE"
    assert result.endpoint == "replace"
    assert len(result.payload["cardsToReplace"]) == 3


def test_betting_can_select_check() -> None:
    service = DecisionService(random_source=Random(3))
    ai_request = build_request(
        valid_actions=[
            {"type": "CHECK", "properties": {}},
            {"type": "FOLD", "properties": {}},
        ],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "CHECK"


def test_betting_can_select_call() -> None:
    service = DecisionService(random_source=Random(17))
    ai_request = build_request(
        hand_type="PAIR",
        my_bet=20,
        opponent_bet=28,
        valid_actions=[
            {"type": "CALL", "properties": {}},
            {"type": "FOLD", "properties": {}},
            {"type": "ALL_IN", "properties": {}},
        ],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "CALL"


def test_betting_can_select_bet_with_legal_amount() -> None:
    service = DecisionService(random_source=Random(9))
    ai_request = build_request(
        aggressivity="HIGH",
        hand_type="FULL_HOUSE",
        valid_actions=[
            {"type": "CHECK", "properties": {}},
            {"type": "BET", "properties": {"min": "30", "max": "120"}},
            {"type": "FOLD", "properties": {}},
        ],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "BET"
    assert 30 <= result.payload["amount"] <= 120


def test_betting_can_select_raise_with_legal_amount() -> None:
    service = DecisionService(random_source=Random(7))
    ai_request = build_request(
        aggressivity="HIGH",
        hand_type="FLUSH",
        my_bet=20,
        opponent_bet=70,
        valid_actions=[
            {"type": "CALL", "properties": {}},
            {"type": "RAISE", "properties": {"min": "90", "max": "220"}},
            {"type": "FOLD", "properties": {}},
            {"type": "ALL_IN", "properties": {}},
        ],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "RAISE"
    assert 90 <= result.payload["amount"] <= 220


def test_betting_can_select_all_in() -> None:
    service = DecisionService(random_source=Random(13))
    ai_request = build_request(
        hand_type="TWO_PAIR",
        my_chips=15,
        my_bet=10,
        opponent_bet=20,
        valid_actions=[
            {"type": "CALL", "properties": {}},
            {"type": "ALL_IN", "properties": {}},
            {"type": "FOLD", "properties": {}},
        ],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "ALL_IN"


def test_betting_can_select_fold_under_high_pressure() -> None:
    service = DecisionService(random_source=Random(19))
    ai_request = build_request(
        hand_type="HIGH_CARD",
        my_chips=30,
        my_bet=0,
        opponent_bet=25,
        valid_actions=[
            {"type": "CALL", "properties": {}},
            {"type": "FOLD", "properties": {}},
            {"type": "ALL_IN", "properties": {}},
        ],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    assert result.action_type == "FOLD"


def test_selected_action_is_always_legal() -> None:
    service = DecisionService(random_source=Random(21))
    ai_request = build_request(
        aggressivity="HIGH",
        hand_type="PAIR",
        my_bet=20,
        opponent_bet=65,
        valid_actions=[
            {"type": "CALL", "properties": {}},
            {"type": "RAISE", "properties": {"min": "85", "max": "200"}},
            {"type": "FOLD", "properties": {}},
            {"type": "ALL_IN", "properties": {}},
        ],
    )

    result = service.choose_action(ai_request)

    assert result is not None
    legal_types = {action.type.value for action in ai_request.gameResponse.validActions}
    assert result.action_type in legal_types


def test_same_seed_produces_same_first_decision() -> None:
    request = build_request(
        aggressivity="HIGH",
        hand_type="PAIR",
        my_bet=15,
        opponent_bet=40,
        valid_actions=[
            {"type": "CALL", "properties": {}},
            {"type": "RAISE", "properties": {"min": "55", "max": "180"}},
            {"type": "FOLD", "properties": {}},
        ],
    )

    service_a = DecisionService(random_source=Random(55))
    service_b = DecisionService(random_source=Random(55))

    result_a = service_a.choose_action(request)
    result_b = service_b.choose_action(request)

    assert result_a is not None and result_b is not None
    assert result_a.action_type == result_b.action_type
    assert result_a.payload == result_b.payload

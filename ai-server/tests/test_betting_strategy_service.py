from random import Random

from app.models.ai_request import Aggressivity, GameResponse
from app.services.betting_strategy_service import BettingStrategyService


def build_game_response(
    valid_actions: list[dict],
    my_bet: int = 0,
    opponent_bet: int = 0,
) -> GameResponse:
    return GameResponse.model_validate(
        {
            "gameId": "game-1",
            "myId": "player-1",
            "myIndex": 0,
            "phase": "FIRST_BETTING",
            "pot": {"chips": 80, "mainPot": {"chips": 80, "winnerIndexes": []}, "sidePots": []},
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
                    "chips": 200,
                    "bet": my_bet,
                    "ready": False,
                    "folded": False,
                    "cards": [
                        {"rank": "ACE", "suit": "HEARTS"},
                        {"rank": "ACE", "suit": "SPADES"},
                        {"rank": "KING", "suit": "CLUBS"},
                        {"rank": "TEN", "suit": "DIAMONDS"},
                        {"rank": "TWO", "suit": "HEARTS"},
                    ],
                    "handType": "PAIR",
                },
                {
                    "index": 1,
                    "name": "Opponent",
                    "chips": 220,
                    "bet": opponent_bet,
                    "ready": False,
                    "folded": False,
                },
            ],
            "validActions": valid_actions,
        }
    )


def test_bet_amount_respects_legal_range() -> None:
    service = BettingStrategyService()
    game_response = build_game_response(
        valid_actions=[{"type": "BET", "properties": {"min": "25", "max": "95"}}]
    )

    decision = service.choose_betting_action(
        game_response=game_response,
        aggressivity=Aggressivity.HIGH,
        hand_strength=0.82,
        random_source=Random(14),
    )

    assert decision is not None
    assert decision.action_type.value == "BET"
    assert decision.amount is not None
    assert 25 <= decision.amount <= 95


def test_raise_amount_respects_legal_range() -> None:
    service = BettingStrategyService()
    game_response = build_game_response(
        valid_actions=[{"type": "RAISE", "properties": {"min": "90", "max": "180"}}],
        my_bet=20,
        opponent_bet=70,
    )

    decision = service.choose_betting_action(
        game_response=game_response,
        aggressivity=Aggressivity.MEDIUM,
        hand_strength=0.76,
        random_source=Random(22),
    )

    assert decision is not None
    assert decision.action_type.value == "RAISE"
    assert decision.amount is not None
    assert 90 <= decision.amount <= 180

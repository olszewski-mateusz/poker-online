from app.models.ai_request import Player
from app.services.hand_strength_service import HandStrengthService


def build_player(hand_type: str, cards: list[dict]) -> Player:
    return Player.model_validate(
        {
            "index": 0,
            "name": "AI",
            "chips": 200,
            "bet": 0,
            "ready": False,
            "folded": False,
            "handType": hand_type,
            "cards": cards,
        }
    )


def test_strong_hand_scores_higher_than_weak_hand() -> None:
    service = HandStrengthService()
    weak = build_player(
        "HIGH_CARD",
        [
            {"rank": "TWO", "suit": "HEARTS"},
            {"rank": "FOUR", "suit": "CLUBS"},
            {"rank": "SEVEN", "suit": "SPADES"},
            {"rank": "NINE", "suit": "DIAMONDS"},
            {"rank": "JACK", "suit": "HEARTS"},
        ],
    )
    strong = build_player(
        "FULL_HOUSE",
        [
            {"rank": "ACE", "suit": "HEARTS"},
            {"rank": "ACE", "suit": "SPADES"},
            {"rank": "ACE", "suit": "CLUBS"},
            {"rank": "KING", "suit": "HEARTS"},
            {"rank": "KING", "suit": "DIAMONDS"},
        ],
    )

    weak_score = service.evaluate(weak)
    strong_score = service.evaluate(strong)

    assert 0.0 <= weak_score <= 1.0
    assert 0.0 <= strong_score <= 1.0
    assert strong_score > weak_score


def test_straight_flush_beats_four_of_a_kind() -> None:
    service = HandStrengthService()
    four_kind = build_player(
        "FOUR_OF_A_KIND",
        [
            {"rank": "NINE", "suit": "HEARTS"},
            {"rank": "NINE", "suit": "SPADES"},
            {"rank": "NINE", "suit": "CLUBS"},
            {"rank": "NINE", "suit": "DIAMONDS"},
            {"rank": "KING", "suit": "HEARTS"},
        ],
    )
    straight_flush = build_player(
        "STRAIGHT_FLUSH",
        [
            {"rank": "TEN", "suit": "HEARTS"},
            {"rank": "JACK", "suit": "HEARTS"},
            {"rank": "QUEEN", "suit": "HEARTS"},
            {"rank": "KING", "suit": "HEARTS"},
            {"rank": "ACE", "suit": "HEARTS"},
        ],
    )

    assert service.evaluate(straight_flush) > service.evaluate(four_kind)

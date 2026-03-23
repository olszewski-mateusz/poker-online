from app.models.ai_request import Card, HandType
from app.services.draw_strategy_service import DrawStrategyService


def cards_to_set(cards: list[Card]) -> set[tuple[str, str]]:
    return {(card.rank.value, card.suit.value) for card in cards}


def test_pair_draw_replaces_non_pair_cards() -> None:
    service = DrawStrategyService()
    cards = [
        Card.model_validate({"rank": "ACE", "suit": "HEARTS"}),
        Card.model_validate({"rank": "ACE", "suit": "SPADES"}),
        Card.model_validate({"rank": "THREE", "suit": "CLUBS"}),
        Card.model_validate({"rank": "SEVEN", "suit": "DIAMONDS"}),
        Card.model_validate({"rank": "NINE", "suit": "HEARTS"}),
    ]

    replacements = service.choose_cards_to_replace(
        cards,
        HandType.PAIR,
        min_replace=0,
        max_replace=5,
    )

    replaced = cards_to_set(replacements)
    assert len(replacements) == 3
    assert ("ACE", "HEARTS") not in replaced
    assert ("ACE", "SPADES") not in replaced


def test_flush_draw_keeps_four_of_same_suit() -> None:
    service = DrawStrategyService()
    cards = [
        Card.model_validate({"rank": "TWO", "suit": "HEARTS"}),
        Card.model_validate({"rank": "SIX", "suit": "HEARTS"}),
        Card.model_validate({"rank": "NINE", "suit": "HEARTS"}),
        Card.model_validate({"rank": "KING", "suit": "HEARTS"}),
        Card.model_validate({"rank": "FOUR", "suit": "CLUBS"}),
    ]

    replacements = service.choose_cards_to_replace(
        cards,
        HandType.HIGH_CARD,
        min_replace=0,
        max_replace=5,
    )

    assert len(replacements) == 1
    assert replacements[0].suit.value == "CLUBS"


def test_straight_draw_keeps_consecutive_four_cards() -> None:
    service = DrawStrategyService()
    cards = [
        Card.model_validate({"rank": "FIVE", "suit": "HEARTS"}),
        Card.model_validate({"rank": "SIX", "suit": "CLUBS"}),
        Card.model_validate({"rank": "SEVEN", "suit": "SPADES"}),
        Card.model_validate({"rank": "EIGHT", "suit": "DIAMONDS"}),
        Card.model_validate({"rank": "KING", "suit": "HEARTS"}),
    ]

    replacements = service.choose_cards_to_replace(
        cards,
        HandType.HIGH_CARD,
        min_replace=0,
        max_replace=5,
    )

    assert len(replacements) == 1
    assert replacements[0].rank.value == "KING"


def test_replace_count_respects_min_and_max_bounds() -> None:
    service = DrawStrategyService()
    cards = [
        Card.model_validate({"rank": "ACE", "suit": "HEARTS"}),
        Card.model_validate({"rank": "ACE", "suit": "SPADES"}),
        Card.model_validate({"rank": "THREE", "suit": "CLUBS"}),
        Card.model_validate({"rank": "SEVEN", "suit": "DIAMONDS"}),
        Card.model_validate({"rank": "NINE", "suit": "HEARTS"}),
    ]

    replacements = service.choose_cards_to_replace(
        cards,
        HandType.PAIR,
        min_replace=2,
        max_replace=2,
    )

    assert len(replacements) == 2

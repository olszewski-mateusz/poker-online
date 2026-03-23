from collections import Counter

from app.models.ai_request import Card, HandType, Player, Rank

_RANK_TO_VALUE: dict[Rank, int] = {
    Rank.TWO: 2,
    Rank.THREE: 3,
    Rank.FOUR: 4,
    Rank.FIVE: 5,
    Rank.SIX: 6,
    Rank.SEVEN: 7,
    Rank.EIGHT: 8,
    Rank.NINE: 9,
    Rank.TEN: 10,
    Rank.JACK: 11,
    Rank.QUEEN: 12,
    Rank.KING: 13,
    Rank.ACE: 14,
}

_HAND_TYPE_BASE_STRENGTH: dict[HandType, float] = {
    HandType.HIGH_CARD: 0.18,
    HandType.PAIR: 0.32,
    HandType.TWO_PAIR: 0.48,
    HandType.THREE_OF_A_KIND: 0.60,
    HandType.STRAIGHT: 0.72,
    HandType.FLUSH: 0.78,
    HandType.FULL_HOUSE: 0.88,
    HandType.FOUR_OF_A_KIND: 0.96,
    HandType.STRAIGHT_FLUSH: 1.00,
}


class HandStrengthService:
    def evaluate(self, me: Player | None) -> float:
        if me is None:
            return 0.30

        cards = me.cards or []
        hand_type = me.handType or self._infer_hand_type(cards)
        base_strength = _HAND_TYPE_BASE_STRENGTH.get(hand_type, 0.18)
        rank_strength = self._rank_strength(cards)
        draw_bonus = self._draw_potential_bonus(cards, hand_type)
        return self._clamp(base_strength * 0.78 + rank_strength * 0.22 + draw_bonus)

    def _infer_hand_type(self, cards: list[Card]) -> HandType:
        if len(cards) < 5:
            return HandType.HIGH_CARD

        rank_counts = sorted(Counter(card.rank for card in cards).values(), reverse=True)
        is_flush = len({card.suit for card in cards}) == 1
        is_straight = self._is_straight(cards)

        if is_flush and is_straight:
            return HandType.STRAIGHT_FLUSH
        if rank_counts == [4, 1]:
            return HandType.FOUR_OF_A_KIND
        if rank_counts == [3, 2]:
            return HandType.FULL_HOUSE
        if is_flush:
            return HandType.FLUSH
        if is_straight:
            return HandType.STRAIGHT
        if rank_counts == [3, 1, 1]:
            return HandType.THREE_OF_A_KIND
        if rank_counts == [2, 2, 1]:
            return HandType.TWO_PAIR
        if rank_counts == [2, 1, 1, 1]:
            return HandType.PAIR
        return HandType.HIGH_CARD

    def _rank_strength(self, cards: list[Card]) -> float:
        if not cards:
            return 0.0

        values = sorted((_RANK_TO_VALUE[card.rank] for card in cards), reverse=True)
        weights = [0.52, 0.24, 0.14, 0.07, 0.03]
        weighted_sum = 0.0
        for i, value in enumerate(values[:5]):
            weighted_sum += (value / 14.0) * weights[i]
        return self._clamp(weighted_sum)

    def _draw_potential_bonus(self, cards: list[Card], hand_type: HandType) -> float:
        if hand_type != HandType.HIGH_CARD:
            return 0.0

        suit_counts = Counter(card.suit for card in cards)
        if any(count >= 4 for count in suit_counts.values()):
            return 0.05

        if self._is_four_to_straight(cards):
            return 0.04

        return 0.0

    def _is_straight(self, cards: list[Card]) -> bool:
        values = sorted({_RANK_TO_VALUE[card.rank] for card in cards})
        if len(values) != 5:
            return False
        if values[-1] - values[0] == 4:
            return True
        return values == [2, 3, 4, 5, 14]

    def _is_four_to_straight(self, cards: list[Card]) -> bool:
        values = sorted({_RANK_TO_VALUE[card.rank] for card in cards})
        if len(values) < 4:
            return False

        if 14 in values:
            values.append(1)

        values = sorted(set(values))
        for i in range(len(values)):
            for j in range(i + 3, len(values)):
                segment = values[i : j + 1]
                if len(segment) == 4 and segment[-1] - segment[0] <= 4:
                    return True
        return False

    @staticmethod
    def _clamp(value: float, minimum: float = 0.0, maximum: float = 1.0) -> float:
        return max(minimum, min(maximum, value))

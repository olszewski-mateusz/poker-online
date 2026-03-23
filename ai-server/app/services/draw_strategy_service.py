from collections import Counter
from itertools import combinations

from app.models.ai_request import Card, HandType, Rank

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

_STRONG_MADE_HANDS = {
    HandType.STRAIGHT,
    HandType.FLUSH,
    HandType.FULL_HOUSE,
    HandType.FOUR_OF_A_KIND,
    HandType.STRAIGHT_FLUSH,
}


class DrawStrategyService:
    def choose_cards_to_replace(
        self,
        cards: list[Card] | None,
        hand_type: HandType | None,
        min_replace: int,
        max_replace: int,
    ) -> list[Card]:
        if not cards:
            return []

        normalized_min = max(0, min_replace)
        normalized_max = max(normalized_min, max_replace)

        keep_indices = self._choose_keep_indices(cards, hand_type)
        candidate_indices = [index for index in range(len(cards)) if index not in keep_indices]
        candidate_indices.sort(key=lambda idx: _RANK_TO_VALUE[cards[idx].rank])

        desired_count = self._clamp(len(candidate_indices), normalized_min, normalized_max)
        if desired_count < len(candidate_indices):
            candidate_indices = candidate_indices[:desired_count]
        elif desired_count > len(candidate_indices):
            additional = [index for index in range(len(cards)) if index not in candidate_indices]
            additional.sort(key=lambda idx: _RANK_TO_VALUE[cards[idx].rank])
            candidate_indices.extend(additional[: desired_count - len(candidate_indices)])

        return [cards[index] for index in candidate_indices]

    def _choose_keep_indices(self, cards: list[Card], hand_type: HandType | None) -> set[int]:
        rank_to_indices = self._group_rank_indices(cards)
        if hand_type in _STRONG_MADE_HANDS:
            return set(range(len(cards)))

        if hand_type == HandType.THREE_OF_A_KIND:
            return self._keep_of_count(rank_to_indices, 3)

        if hand_type == HandType.TWO_PAIR:
            return self._keep_of_count(rank_to_indices, 2)

        if hand_type == HandType.PAIR:
            keep_pair = self._keep_of_count(rank_to_indices, 2)
            if keep_pair:
                return keep_pair

        flush_draw = self._flush_draw_keep_indices(cards)
        if len(flush_draw) == 4:
            return flush_draw

        straight_draw = self._straight_draw_keep_indices(cards)
        if len(straight_draw) == 4:
            return straight_draw

        return self._keep_best_high_cards(cards)

    def _group_rank_indices(self, cards: list[Card]) -> dict[Rank, list[int]]:
        grouped: dict[Rank, list[int]] = {}
        for index, card in enumerate(cards):
            grouped.setdefault(card.rank, []).append(index)
        return grouped

    def _keep_of_count(self, rank_to_indices: dict[Rank, list[int]], count: int) -> set[int]:
        kept: set[int] = set()
        for indices in rank_to_indices.values():
            if len(indices) == count:
                kept.update(indices)
        return kept

    def _flush_draw_keep_indices(self, cards: list[Card]) -> set[int]:
        suit_counts = Counter(card.suit for card in cards)
        flush_suit = next((suit for suit, count in suit_counts.items() if count >= 4), None)
        if flush_suit is None:
            return set()
        return {index for index, card in enumerate(cards) if card.suit == flush_suit}

    def _straight_draw_keep_indices(self, cards: list[Card]) -> set[int]:
        for combo in combinations(range(len(cards)), 4):
            if self._is_straight_like([cards[i] for i in combo]):
                return set(combo)
        return set()

    def _is_straight_like(self, cards_subset: list[Card]) -> bool:
        values = sorted({_RANK_TO_VALUE[card.rank] for card in cards_subset})
        if len(values) != 4:
            return False
        if values[-1] - values[0] <= 4:
            return True
        if 14 in values:
            low_ace = sorted(1 if value == 14 else value for value in values)
            return low_ace[-1] - low_ace[0] <= 4
        return False

    def _keep_best_high_cards(self, cards: list[Card]) -> set[int]:
        ranked = sorted(
            range(len(cards)),
            key=lambda idx: _RANK_TO_VALUE[cards[idx].rank],
            reverse=True,
        )
        keep_count = 2 if _RANK_TO_VALUE[cards[ranked[0]].rank] >= 12 else 1
        return set(ranked[:keep_count])

    @staticmethod
    def _clamp(value: int, minimum: int, maximum: int) -> int:
        return max(minimum, min(maximum, value))

from dataclasses import dataclass
from random import Random

from app.models.ai_request import ActionType, Aggressivity, GameResponse, Player


@dataclass(frozen=True, slots=True)
class BettingDecision:
    action_type: ActionType
    amount: int | None = None


@dataclass(frozen=True, slots=True)
class _ScoredDecision:
    decision: BettingDecision
    score: float


class BettingStrategyService:
    _AGGRESSIVITY_FACTOR: dict[Aggressivity, float] = {
        Aggressivity.LOW: 0.85,
        Aggressivity.MEDIUM: 1.00,
        Aggressivity.HIGH: 1.18,
    }
    _BLUFF_CHANCE: dict[Aggressivity, float] = {
        Aggressivity.LOW: 0.03,
        Aggressivity.MEDIUM: 0.06,
        Aggressivity.HIGH: 0.10,
    }
    _BET_SIZE_FACTOR: dict[Aggressivity, float] = {
        Aggressivity.LOW: 0.32,
        Aggressivity.MEDIUM: 0.52,
        Aggressivity.HIGH: 0.72,
    }

    def choose_betting_action(
        self,
        game_response: GameResponse,
        aggressivity: Aggressivity,
        hand_strength: float,
        random_source: Random,
    ) -> BettingDecision | None:
        actions_by_type = {action.type: action for action in game_response.validActions}
        me = self._find_me(game_response)

        if me is None:
            return None

        stack = max(1, me.chips)
        max_bet = max((player.bet for player in game_response.players), default=me.bet)
        amount_to_call = max(0, max_bet - me.bet)
        call_pressure = min(1.0, amount_to_call / stack)
        pot_size = game_response.pot.chips if game_response.pot else 0
        pot_pressure = min(1.0, pot_size / stack)
        aggressivity_factor = self._AGGRESSIVITY_FACTOR[aggressivity]
        bluff_chance = self._BLUFF_CHANCE[aggressivity] * max(0.2, 1.0 - hand_strength)
        bluff_active = random_source.random() < bluff_chance
        if game_response.configuration:
            short_stack_threshold = max(20, (game_response.configuration.ante or 5) * 6)
        else:
            short_stack_threshold = 30
        short_stack = me.chips <= short_stack_threshold

        candidates: list[_ScoredDecision] = []

        if ActionType.CHECK in actions_by_type:
            score = (
                0.42
                + (0.35 * (0.58 - hand_strength))
                + (0.10 * (1.0 - aggressivity_factor))
                + random_source.uniform(-0.08, 0.08)
            )
            candidates.append(
                _ScoredDecision(BettingDecision(ActionType.CHECK), self._ensure_positive(score))
            )

        if ActionType.CALL in actions_by_type:
            score = (
                0.22
                + hand_strength * 0.72
                + aggressivity_factor * 0.08
                - call_pressure * 0.72
                + random_source.uniform(-0.08, 0.08)
            )
            candidates.append(
                _ScoredDecision(BettingDecision(ActionType.CALL), self._ensure_positive(score))
            )

        if ActionType.FOLD in actions_by_type:
            score = (
                0.06
                + (1.0 - hand_strength) * 0.95
                + call_pressure * 1.08
                - aggressivity_factor * 0.10
                + random_source.uniform(-0.06, 0.06)
            )
            if amount_to_call == 0:
                score -= 1.25
            candidates.append(
                _ScoredDecision(BettingDecision(ActionType.FOLD), self._ensure_positive(score))
            )

        if ActionType.BET in actions_by_type:
            bet_action = actions_by_type[ActionType.BET]
            bet_amount = self._choose_sized_amount(
                properties=bet_action.properties,
                aggressivity=aggressivity,
                hand_strength=hand_strength,
                call_pressure=call_pressure,
                pot_pressure=pot_pressure,
                random_source=random_source,
            )
            if bet_amount is not None:
                score = (
                    0.14
                    + hand_strength * 0.90
                    + aggressivity_factor * 0.18
                    + pot_pressure * 0.15
                    + (0.24 if bluff_active else 0.0)
                    + random_source.uniform(-0.08, 0.08)
                )
                candidates.append(
                    _ScoredDecision(
                        BettingDecision(ActionType.BET, amount=bet_amount),
                        self._ensure_positive(score),
                    )
                )

        if ActionType.RAISE in actions_by_type:
            raise_action = actions_by_type[ActionType.RAISE]
            raise_amount = self._choose_sized_amount(
                properties=raise_action.properties,
                aggressivity=aggressivity,
                hand_strength=min(1.0, hand_strength + 0.04),
                call_pressure=call_pressure,
                pot_pressure=pot_pressure,
                random_source=random_source,
            )
            if raise_amount is not None:
                score = (
                    0.10
                    + hand_strength * 0.98
                    + call_pressure * 0.12
                    + aggressivity_factor * 0.22
                    + (0.20 if bluff_active else 0.0)
                    + random_source.uniform(-0.08, 0.08)
                )
                candidates.append(
                    _ScoredDecision(
                        BettingDecision(ActionType.RAISE, amount=raise_amount),
                        self._ensure_positive(score),
                    )
                )

        if ActionType.ALL_IN in actions_by_type:
            score = (
                -0.15
                + (hand_strength * hand_strength) * 1.45
                + (0.45 if short_stack and hand_strength > 0.45 else 0.0)
                + (0.16 if bluff_active and aggressivity == Aggressivity.HIGH else 0.0)
                + random_source.uniform(-0.05, 0.05)
            )
            if hand_strength < 0.35 and not short_stack:
                score -= 0.30
            candidates.append(
                _ScoredDecision(BettingDecision(ActionType.ALL_IN), self._ensure_positive(score))
            )

        if not candidates:
            return None

        max_score = max(candidate.score for candidate in candidates)
        top_candidates = [
            candidate for candidate in candidates if candidate.score >= max_score * 0.72
        ]
        if len(top_candidates) == 1:
            return top_candidates[0].decision

        return self._weighted_choice(top_candidates, random_source).decision

    def _choose_sized_amount(
        self,
        properties: dict[str, str],
        aggressivity: Aggressivity,
        hand_strength: float,
        call_pressure: float,
        pot_pressure: float,
        random_source: Random,
    ) -> int | None:
        min_amount = self._parse_int(properties.get("min"))
        max_amount = self._parse_int(properties.get("max"))
        if min_amount is None:
            return None

        if max_amount is None:
            max_amount = min_amount

        lower = min(min_amount, max_amount)
        upper = max(min_amount, max_amount)
        if lower == upper:
            return lower

        ratio = (
            hand_strength * 0.62
            + self._BET_SIZE_FACTOR[aggressivity] * 0.24
            + pot_pressure * 0.10
            + call_pressure * 0.07
            + random_source.uniform(-0.10, 0.10)
        )
        ratio = self._clamp(ratio)
        amount = int(round(lower + (upper - lower) * ratio))
        return max(lower, min(upper, amount))

    def _find_me(self, game_response: GameResponse) -> Player | None:
        if game_response.myIndex is not None:
            return next(
                (
                    player
                    for player in game_response.players
                    if player.index == game_response.myIndex
                ),
                None,
            )
        return game_response.players[0] if game_response.players else None

    def _weighted_choice(
        self,
        candidates: list[_ScoredDecision],
        random_source: Random,
    ) -> _ScoredDecision:
        total = sum(candidate.score for candidate in candidates)
        pick = random_source.uniform(0.0, total)
        running = 0.0
        for candidate in candidates:
            running += candidate.score
            if running >= pick:
                return candidate
        return candidates[-1]

    @staticmethod
    def _parse_int(value: str | None) -> int | None:
        if value is None:
            return None
        try:
            return int(value)
        except ValueError:
            return None

    @staticmethod
    def _ensure_positive(value: float) -> float:
        return max(0.01, value)

    @staticmethod
    def _clamp(value: float, minimum: float = 0.0, maximum: float = 1.0) -> float:
        return max(minimum, min(maximum, value))

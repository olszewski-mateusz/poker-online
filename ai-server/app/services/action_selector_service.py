from random import Random

from app.models.ai_request import ActionType, AiRequest, GamePhase, Player, ValidAction
from app.services.action_models import PlannedAction
from app.services.betting_strategy_service import BettingDecision, BettingStrategyService
from app.services.draw_strategy_service import DrawStrategyService
from app.services.hand_strength_service import HandStrengthService


class ActionSelectorService:
    _FALLBACK_PRIORITY = (
        ActionType.CHECK,
        ActionType.CALL,
        ActionType.BET,
        ActionType.RAISE,
        ActionType.ALL_IN,
        ActionType.FOLD,
        ActionType.REPLACE,
    )

    def __init__(
        self,
        hand_strength_service: HandStrengthService,
        draw_strategy_service: DrawStrategyService,
        betting_strategy_service: BettingStrategyService,
    ) -> None:
        self._hand_strength_service = hand_strength_service
        self._draw_strategy_service = draw_strategy_service
        self._betting_strategy_service = betting_strategy_service

    def choose_action(self, ai_request: AiRequest, random_source: Random) -> PlannedAction | None:
        game_response = ai_request.gameResponse
        actions_by_type = {action.type: action for action in game_response.validActions}

        ready_action = actions_by_type.get(ActionType.READY)
        if ready_action is not None and ready_action.properties.get("ready", "").lower() == "true":
            return PlannedAction(
                game_id=game_response.gameId,
                action_type=ActionType.READY.value,
                endpoint="ready",
                payload={"playerId": game_response.myId, "ready": True},
            )

        me = self._find_me(game_response.players, game_response.myIndex)
        hand_strength = self._hand_strength_service.evaluate(me)

        if game_response.phase == GamePhase.DRAWING and ActionType.REPLACE in actions_by_type:
            replace_action = actions_by_type[ActionType.REPLACE]
            min_replace = self._parse_int(replace_action.properties.get("min")) or 0
            max_replace = self._parse_int(replace_action.properties.get("max")) or 5
            cards_to_replace = self._draw_strategy_service.choose_cards_to_replace(
                cards=me.cards if me else None,
                hand_type=me.handType if me else None,
                min_replace=min_replace,
                max_replace=max_replace,
            )
            return PlannedAction(
                game_id=game_response.gameId,
                action_type=ActionType.REPLACE.value,
                endpoint="replace",
                payload={
                    "playerId": game_response.myId,
                    "cardsToReplace": [card.model_dump(mode="json") for card in cards_to_replace],
                },
            )

        if game_response.phase in {GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING}:
            betting_decision = self._betting_strategy_service.choose_betting_action(
                game_response=game_response,
                aggressivity=ai_request.aiConfiguration.aggressivity,
                hand_strength=hand_strength,
                random_source=random_source,
            )
            if betting_decision is not None:
                planned_action = self._decision_to_planned_action(
                    game_id=game_response.gameId,
                    player_id=game_response.myId,
                    decision=betting_decision,
                )
                if planned_action is not None:
                    return planned_action

        return self._fallback(
            game_id=game_response.gameId,
            player_id=game_response.myId,
            actions_by_type=actions_by_type,
        )

    def _fallback(
        self,
        game_id: str,
        player_id: str,
        actions_by_type: dict[ActionType, ValidAction],
    ) -> PlannedAction | None:
        for action_type in self._FALLBACK_PRIORITY:
            if action_type not in actions_by_type:
                continue

            if action_type in {
                ActionType.CHECK,
                ActionType.CALL,
                ActionType.ALL_IN,
                ActionType.FOLD,
            }:
                endpoint = {
                    ActionType.CHECK: "check",
                    ActionType.CALL: "call",
                    ActionType.ALL_IN: "all-in",
                    ActionType.FOLD: "fold",
                }[action_type]
                return PlannedAction(
                    game_id=game_id,
                    action_type=action_type.value,
                    endpoint=endpoint,
                    payload={"playerId": player_id},
                )

            if action_type in {ActionType.BET, ActionType.RAISE}:
                action = actions_by_type[action_type]
                min_amount = self._parse_int(action.properties.get("min")) if action else None
                if min_amount is None:
                    continue
                endpoint = "bet" if action_type == ActionType.BET else "raise"
                return PlannedAction(
                    game_id=game_id,
                    action_type=action_type.value,
                    endpoint=endpoint,
                    payload={"playerId": player_id, "amount": min_amount},
                )

            if action_type == ActionType.REPLACE:
                return PlannedAction(
                    game_id=game_id,
                    action_type=action_type.value,
                    endpoint="replace",
                    payload={"playerId": player_id, "cardsToReplace": []},
                )

        return None

    def _decision_to_planned_action(
        self,
        game_id: str,
        player_id: str,
        decision: BettingDecision,
    ) -> PlannedAction | None:
        if decision.action_type in {
            ActionType.CHECK,
            ActionType.CALL,
            ActionType.ALL_IN,
            ActionType.FOLD,
        }:
            endpoint = {
                ActionType.CHECK: "check",
                ActionType.CALL: "call",
                ActionType.ALL_IN: "all-in",
                ActionType.FOLD: "fold",
            }[decision.action_type]
            return PlannedAction(
                game_id=game_id,
                action_type=decision.action_type.value,
                endpoint=endpoint,
                payload={"playerId": player_id},
            )

        if (
            decision.action_type in {ActionType.BET, ActionType.RAISE}
            and decision.amount is not None
        ):
            endpoint = "bet" if decision.action_type == ActionType.BET else "raise"
            return PlannedAction(
                game_id=game_id,
                action_type=decision.action_type.value,
                endpoint=endpoint,
                payload={"playerId": player_id, "amount": decision.amount},
            )

        return None

    @staticmethod
    def _find_me(players: list[Player], my_index: int | None) -> Player | None:
        if my_index is not None:
            for player in players:
                if player.index == my_index:
                    return player
        return players[0] if players else None

    @staticmethod
    def _parse_int(value: str | None) -> int | None:
        if value is None:
            return None
        try:
            return int(value)
        except ValueError:
            return None

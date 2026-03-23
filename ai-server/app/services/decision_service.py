from random import Random

from app.models.ai_request import AiRequest
from app.services.action_models import PlannedAction
from app.services.action_selector_service import ActionSelectorService
from app.services.betting_strategy_service import BettingStrategyService
from app.services.draw_strategy_service import DrawStrategyService
from app.services.hand_strength_service import HandStrengthService


class DecisionService:
    def __init__(
        self,
        random_source: Random | None = None,
        hand_strength_service: HandStrengthService | None = None,
        draw_strategy_service: DrawStrategyService | None = None,
        betting_strategy_service: BettingStrategyService | None = None,
        action_selector_service: ActionSelectorService | None = None,
    ) -> None:
        self._random_source = random_source or Random()
        self._action_selector_service = action_selector_service or ActionSelectorService(
            hand_strength_service=hand_strength_service or HandStrengthService(),
            draw_strategy_service=draw_strategy_service or DrawStrategyService(),
            betting_strategy_service=betting_strategy_service or BettingStrategyService(),
        )

    def choose_action(self, ai_request: AiRequest) -> PlannedAction | None:
        return self._action_selector_service.choose_action(
            ai_request,
            random_source=self._random_source,
        )

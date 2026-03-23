import logging
import random
from asyncio import sleep as asyncio_sleep
from collections.abc import Awaitable, Callable

from app.models.ai_request import AiRequest
from app.services.decision_service import DecisionService
from app.services.server_client import PokerServerClient

logger = logging.getLogger(__name__)


class AiRequestProcessor:
    def __init__(
        self,
        decision_service: DecisionService,
        poker_server_client: PokerServerClient,
        min_delay_seconds: float = 2.0,
        max_delay_seconds: float = 3.0,
        sleep: Callable[[float], Awaitable[None]] = asyncio_sleep,
        randomizer: Callable[[float, float], float] = random.uniform,
    ) -> None:
        self._decision_service = decision_service
        self._poker_server_client = poker_server_client
        self._min_delay_seconds = max(0.0, min_delay_seconds)
        self._max_delay_seconds = max(self._min_delay_seconds, max_delay_seconds)
        self._sleep = sleep
        self._randomizer = randomizer

    async def process(self, ai_request: AiRequest) -> None:
        planned_action = self._decision_service.choose_action(ai_request)
        if planned_action is None:
            logger.debug(
                "No valid AI action selected for gameId=%s playerId=%s",
                ai_request.gameResponse.gameId,
                ai_request.gameResponse.myId,
            )
            return

        try:
            delay_seconds = self._randomizer(self._min_delay_seconds, self._max_delay_seconds)
            logger.debug(
                "Waiting %.2fs before dispatching action gameId=%s playerId=%s action=%s",
                delay_seconds,
                planned_action.game_id,
                ai_request.gameResponse.myId,
                planned_action.action_type,
            )
            await self._sleep(delay_seconds)
            await self._poker_server_client.send_action(planned_action)
            logger.info(
                "AI action sent gameId=%s playerId=%s action=%s",
                planned_action.game_id,
                ai_request.gameResponse.myId,
                planned_action.action_type,
            )
        except Exception:
            logger.exception(
                "AI action dispatch failed gameId=%s playerId=%s action=%s",
                planned_action.game_id,
                ai_request.gameResponse.myId,
                planned_action.action_type,
            )

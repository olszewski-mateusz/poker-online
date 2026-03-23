import logging

from app.models.ai_request import AiRequest
from app.services.decision_service import DecisionService
from app.services.server_client import PokerServerClient

logger = logging.getLogger(__name__)


class AiRequestProcessor:
    def __init__(self, decision_service: DecisionService, poker_server_client: PokerServerClient) -> None:
        self._decision_service = decision_service
        self._poker_server_client = poker_server_client

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

import logging

import pytest

from app.models.ai_request import AiRequest
from app.services.decision_service import PlannedAction
from app.services.processor import AiRequestProcessor


class StubDecisionService:
    def choose_action(self, ai_request: AiRequest) -> PlannedAction:
        return PlannedAction(
            game_id=ai_request.gameResponse.gameId,
            action_type="CHECK",
            endpoint="check",
            payload={"playerId": ai_request.gameResponse.myId},
        )


class FailingServerClient:
    async def send_action(self, action: PlannedAction) -> None:
        raise RuntimeError("network failed")


@pytest.mark.asyncio
async def test_processor_logs_and_swallows_dispatch_errors(caplog: pytest.LogCaptureFixture) -> None:
    processor = AiRequestProcessor(
        decision_service=StubDecisionService(),
        poker_server_client=FailingServerClient(),
    )
    ai_request = AiRequest.model_validate(
        {
            "aiConfiguration": {"aggressivity": "MEDIUM"},
            "gameResponse": {
                "gameId": "game-1",
                "myId": "player-1",
                "validActions": [{"type": "CHECK", "properties": {}}],
            },
        }
    )

    with caplog.at_level(logging.ERROR):
        await processor.process(ai_request)

    assert "AI action dispatch failed" in caplog.text

import logging

import pytest

from app.models.ai_request import AiRequest
from app.services.action_models import PlannedAction
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


class SuccessfulServerClient:
    def __init__(self) -> None:
        self.call_count = 0

    async def send_action(self, action: PlannedAction) -> None:
        self.call_count += 1


def build_request() -> AiRequest:
    return AiRequest.model_validate(
        {
            "aiConfiguration": {"aggressivity": "MEDIUM"},
            "gameResponse": {
                "gameId": "game-1",
                "myId": "player-1",
                "validActions": [{"type": "CHECK", "properties": {}}],
            },
        }
    )


@pytest.mark.asyncio
async def test_processor_logs_and_swallows_dispatch_errors(
    caplog: pytest.LogCaptureFixture,
) -> None:
    processor = AiRequestProcessor(
        decision_service=StubDecisionService(),
        poker_server_client=FailingServerClient(),
        min_delay_seconds=0.0,
        max_delay_seconds=0.0,
    )
    ai_request = build_request()

    with caplog.at_level(logging.ERROR):
        await processor.process(ai_request)

    assert "AI action dispatch failed" in caplog.text


@pytest.mark.asyncio
async def test_processor_waits_before_dispatching_action() -> None:
    recorded_delay: list[float] = []
    server_client = SuccessfulServerClient()

    async def fake_sleep(seconds: float) -> None:
        recorded_delay.append(seconds)

    processor = AiRequestProcessor(
        decision_service=StubDecisionService(),
        poker_server_client=server_client,
        min_delay_seconds=2.0,
        max_delay_seconds=3.0,
        sleep=fake_sleep,
        randomizer=lambda a, b: 2.4,
    )

    await processor.process(build_request())

    assert recorded_delay == [2.4]
    assert server_client.call_count == 1

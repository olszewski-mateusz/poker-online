import json

import httpx
import pytest
import respx

from app.services.decision_service import PlannedAction
from app.services.server_client import PokerServerClient


@pytest.mark.asyncio
@respx.mock
async def test_send_action_posts_to_expected_endpoint() -> None:
    route = respx.post("http://server/api/game/game-1/action/check").mock(
        return_value=httpx.Response(200)
    )

    async with httpx.AsyncClient(base_url="http://server") as http_client:
        client = PokerServerClient(http_client=http_client)
        await client.send_action(
            PlannedAction(
                game_id="game-1",
                action_type="CHECK",
                endpoint="check",
                payload={"playerId": "player-1"},
            )
        )

    assert route.called
    sent_payload = json.loads(route.calls[0].request.content.decode("utf-8"))
    assert sent_payload == {"playerId": "player-1"}


@pytest.mark.asyncio
@respx.mock
async def test_send_action_raises_for_non_success_status() -> None:
    respx.post("http://server/api/game/game-1/action/fold").mock(return_value=httpx.Response(400))

    async with httpx.AsyncClient(base_url="http://server") as http_client:
        client = PokerServerClient(http_client=http_client)
        with pytest.raises(httpx.HTTPStatusError):
            await client.send_action(
                PlannedAction(
                    game_id="game-1",
                    action_type="FOLD",
                    endpoint="fold",
                    payload={"playerId": "player-1"},
                )
            )


@pytest.mark.asyncio
@respx.mock
async def test_send_action_propagates_timeout() -> None:
    respx.post("http://server/api/game/game-1/action/call").mock(
        side_effect=httpx.ReadTimeout("timed out")
    )

    async with httpx.AsyncClient(base_url="http://server") as http_client:
        client = PokerServerClient(http_client=http_client)
        with pytest.raises(httpx.ReadTimeout):
            await client.send_action(
                PlannedAction(
                    game_id="game-1",
                    action_type="CALL",
                    endpoint="call",
                    payload={"playerId": "player-1"},
                )
            )

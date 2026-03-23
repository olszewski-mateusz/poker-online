import httpx

from app.services.decision_service import PlannedAction


class PokerServerClient:
    def __init__(self, http_client: httpx.AsyncClient) -> None:
        self._http_client = http_client

    async def send_action(self, action: PlannedAction) -> None:
        response = await self._http_client.post(
            f"/api/game/{action.game_id}/action/{action.endpoint}",
            json=action.payload,
        )
        response.raise_for_status()

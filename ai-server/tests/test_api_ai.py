from fastapi.testclient import TestClient

from app.api.ai import get_ai_processor
from app.main import app
from app.models.ai_request import AiRequest


def test_post_ai_returns_200_and_dispatches_in_background(sample_ai_request_payload: dict) -> None:
    calls: list[AiRequest] = []

    class FakeProcessor:
        async def process(self, ai_request: AiRequest) -> None:
            calls.append(ai_request)

    app.dependency_overrides[get_ai_processor] = lambda: FakeProcessor()

    try:
        with TestClient(app) as client:
            response = client.post("/ai", json=sample_ai_request_payload)

        assert response.status_code == 200
        assert response.content == b""
        assert len(calls) == 1
        assert calls[0].gameResponse.gameId == "game-1"
        assert calls[0].gameResponse.myId == "player-1"
    finally:
        app.dependency_overrides.clear()

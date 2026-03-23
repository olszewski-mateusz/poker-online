import pytest


@pytest.fixture
def sample_ai_request_payload() -> dict:
    return {
        "aiConfiguration": {"aggressivity": "MEDIUM"},
        "gameResponse": {
            "gameId": "game-1",
            "myId": "player-1",
            "validActions": [
                {"type": "CHECK", "properties": {}},
            ],
        },
    }

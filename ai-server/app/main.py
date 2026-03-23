import logging
from contextlib import asynccontextmanager

import httpx
from fastapi import FastAPI, Response, status

from app.api.ai import router as ai_router
from app.core.settings import get_settings
from app.services.decision_service import DecisionService
from app.services.processor import AiRequestProcessor
from app.services.server_client import PokerServerClient


def configure_logging(log_level: str) -> None:
    logging.basicConfig(
        level=log_level.upper(),
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
    )


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    configure_logging(settings.log_level)

    timeout = httpx.Timeout(settings.request_timeout_seconds)
    async with httpx.AsyncClient(base_url=settings.poker_server_url, timeout=timeout) as http_client:
        app.state.ai_processor = AiRequestProcessor(
            decision_service=DecisionService(),
            poker_server_client=PokerServerClient(http_client=http_client),
        )
        yield


app = FastAPI(
    title="Poker AI Server",
    description="Receives AI game snapshots and calls poker actions asynchronously.",
    version="0.1.0",
    lifespan=lifespan,
)
app.include_router(ai_router)


@app.get("/healthz", status_code=status.HTTP_200_OK, response_class=Response)
async def healthz() -> Response:
    return Response(status_code=status.HTTP_200_OK)


if __name__ == "__main__":
    import uvicorn

    settings = get_settings()
    uvicorn.run("app.main:app", host=settings.app_host, port=settings.app_port)

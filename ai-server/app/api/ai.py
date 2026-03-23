from fastapi import APIRouter, BackgroundTasks, Depends, Request, Response, status

from app.models.ai_request import AiRequest
from app.services.processor import AiRequestProcessor

router = APIRouter()


def get_ai_processor(request: Request) -> AiRequestProcessor:
    return request.app.state.ai_processor


@router.post("/ai", status_code=status.HTTP_200_OK, response_class=Response)
async def handle_ai_request(
    ai_request: AiRequest,
    background_tasks: BackgroundTasks,
    processor: AiRequestProcessor = Depends(get_ai_processor),
) -> Response:
    background_tasks.add_task(processor.process, ai_request)
    return Response(status_code=status.HTTP_200_OK)

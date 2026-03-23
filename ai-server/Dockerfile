FROM python:3.13-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    UV_LINK_MODE=copy \
    UV_COMPILE_BYTECODE=1

WORKDIR /opt/ai-server

COPY --from=ghcr.io/astral-sh/uv:0.6.14 /uv /uvx /usr/local/bin/

RUN groupadd --system app && useradd --system --gid app --create-home app

COPY pyproject.toml README.md ./
RUN uv sync --no-dev

COPY app ./app

USER app

EXPOSE 8081

CMD ["uv", "run", "--no-dev", "python", "-m", "app.main"]

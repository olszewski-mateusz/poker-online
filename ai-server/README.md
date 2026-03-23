# Poker AI Server

`ai-server` is a FastAPI microservice that receives AI game snapshots from the Java server and sends back one legal poker action.

## Endpoints

- `POST /ai` - accepts `AiRequest`, returns `200` with empty body, dispatches action in background.
- `GET /healthz` - readiness endpoint returning `200`.

## Configuration

- `POKER_SERVER_URL` (default: `http://localhost:8080`)
- `REQUEST_TIMEOUT_SECONDS` (default: `3.0`)
- `AI_ACTION_DELAY_MIN_SECONDS` (default: `2.0`)
- `AI_ACTION_DELAY_MAX_SECONDS` (default: `3.0`)
- `AI_DECISION_SEED` (default: unset; set for reproducible AI decisions in tests/debug)
- `LOG_LEVEL` (default: `INFO`)

## Local Development

1. Install dependencies:
   ```sh
   just install
   ```
2. Run the server:
   ```sh
   just run
   ```
3. Run tests and linting:
   ```sh
   just test
   just lint
   ```

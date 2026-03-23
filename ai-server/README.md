# Poker AI Server

`ai-server` is a FastAPI microservice that receives AI game snapshots from the Java server and sends back one legal poker action.

## Endpoints

- `POST /ai` - accepts `AiRequest`, returns `200` with empty body, dispatches action in background.
- `GET /healthz` - readiness endpoint returning `200`.

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

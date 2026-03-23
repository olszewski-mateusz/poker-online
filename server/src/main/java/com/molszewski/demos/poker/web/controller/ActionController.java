package com.molszewski.demos.poker.web.controller;

import com.molszewski.demos.poker.web.model.request.*;
import com.molszewski.demos.poker.web.model.response.ActionResponse;
import com.molszewski.demos.poker.web.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game/{gameId}/action")
@Slf4j
public class ActionController {
    private final GameService gameService;

    @PostMapping("/join")
    public Mono<ResponseEntity<ActionResponse>> join(@PathVariable String gameId, @RequestBody @Valid JoinRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand(gameService.generatePlayerId()))
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/aijoin")
    public Mono<ResponseEntity<ActionResponse>> aiJoin(@PathVariable String gameId, @RequestBody @Valid AiJoinRequest request) {
        logRequest(gameId, request);
        return gameService.hasAdminAccess(gameId, request.adminSecret()).flatMap(success -> {
            if (success) {
                return gameService.handleCommand(gameId, request.toCommand(gameService.generatePlayerId()))
                        .map(ResponseEntity::ok)
                        .onErrorResume(this::badRequestWithMessage);
            } else {
                return Mono.error(new IllegalArgumentException("Action forbidden"));
            }
        }).onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/remove-player")
    public Mono<ResponseEntity<ActionResponse>> removePlayer(@PathVariable String gameId, @RequestBody @Valid RemovePlayerRequest request) {
        logRequest(gameId, request);
        return gameService.hasAdminAccess(gameId, request.adminSecret()).flatMap(success -> {
            if (!success) {
                return Mono.error(new IllegalArgumentException("Action forbidden"));
            }

            return gameService.handleCommand(gameId, request.toCommand())
                    .map(ResponseEntity::ok)
                    .onErrorResume(this::badRequestWithMessage);
        }).onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/reset-game")
    public Mono<ResponseEntity<ActionResponse>> resetGame(@PathVariable String gameId, @RequestBody @Valid ResetGameRequest request) {
        logRequest(gameId, request);
        return gameService.hasAdminAccess(gameId, request.adminSecret()).flatMap(success -> {
            if (!success) {
                return Mono.error(new IllegalArgumentException("Action forbidden"));
            }

            return Mono.defer(() -> gameService.handleCommand(gameId, request.toCommand()))
                    .map(ResponseEntity::ok)
                    .onErrorResume(this::badRequestWithMessage);
        }).onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/ready")
    public Mono<ResponseEntity<ActionResponse>> ready(@PathVariable String gameId, @RequestBody ReadyRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/check")
    public Mono<ResponseEntity<ActionResponse>> check(@PathVariable String gameId, @RequestBody CheckRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/call")
    public Mono<ResponseEntity<ActionResponse>> call(@PathVariable String gameId, @RequestBody CallRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/bet")
    public Mono<ResponseEntity<ActionResponse>> bet(@PathVariable String gameId, @RequestBody BetRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/all-in")
    public Mono<ResponseEntity<ActionResponse>> allIn(@PathVariable String gameId, @RequestBody AllInRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/fold")
    public Mono<ResponseEntity<ActionResponse>> fold(@PathVariable String gameId, @RequestBody FoldRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/raise")
    public Mono<ResponseEntity<ActionResponse>> raise(@PathVariable String gameId, @RequestBody RaiseRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    @PostMapping("/replace")
    public Mono<ResponseEntity<ActionResponse>> replace(@PathVariable String gameId, @RequestBody ReplaceRequest request) {
        logRequest(gameId, request);
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorResume(this::badRequestWithMessage);
    }

    private <T> void logRequest(String gameId, T request) {
        log.debug("Game: {} Request: {}", gameId, request);
    }

    private Mono<ResponseEntity<ActionResponse>> badRequestWithMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            message = "Bad request";
        }
        return Mono.just(ResponseEntity.badRequest().body(ActionResponse.error(message)));
    }
}

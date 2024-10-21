package com.molszewski.demos.poker.web.controller;

import com.molszewski.demos.poker.web.model.request.*;
import com.molszewski.demos.poker.web.model.response.ActionResponse;
import com.molszewski.demos.poker.web.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/game/{gameId}/action")
public class ActionController {
    private final GameService gameService;

    @PostMapping("/join")
    public Mono<ResponseEntity<ActionResponse>> join(@PathVariable String gameId, @RequestBody JoinRequest request) {
        return gameService.handleCommand(gameId, request.toCommand(gameService.generatePlayerId()))
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/ready")
    public Mono<ResponseEntity<ActionResponse>> ready(@PathVariable String gameId, @RequestBody ReadyRequest request) {
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/check")
    public Mono<ResponseEntity<ActionResponse>> check(@PathVariable String gameId, @RequestBody CheckRequest request) {
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/all-in")
    public Mono<ResponseEntity<ActionResponse>> allIn(@PathVariable String gameId, @RequestBody AllInRequest request) {
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/fold")
    public Mono<ResponseEntity<ActionResponse>> fold(@PathVariable String gameId, @RequestBody FoldRequest request) {
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/raise")
    public Mono<ResponseEntity<ActionResponse>> raise(@PathVariable String gameId, @RequestBody RaiseRequest request) {
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/replace")
    public Mono<ResponseEntity<ActionResponse>> replace(@PathVariable String gameId, @RequestBody ReplaceRequest request) {
        return gameService.handleCommand(gameId, request.toCommand())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}

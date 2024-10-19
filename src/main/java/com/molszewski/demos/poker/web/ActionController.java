package com.molszewski.demos.poker.web;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.web.model.request.*;
import com.molszewski.demos.poker.web.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/game/{gameId}/action")
public class ActionController {
    private final GameService gameService;

    @PostMapping("/join")
    public Mono<Command> join(@PathVariable String gameId, @RequestBody JoinRequest request) {
        return gameService.handleCommand(gameId, request.toCommand(gameService.generatePlayerId()));
    }

    @PostMapping("/ready")
    public Mono<Command> ready(@PathVariable String gameId, @RequestBody ReadyRequest request) {
        return gameService.handleCommand(gameId, request.toCommand());
    }

    @PostMapping("/check")
    public Mono<Command> check(@PathVariable String gameId, @RequestBody CheckRequest request) {
        return gameService.handleCommand(gameId, request.toCommand());
    }

    @PostMapping("/all-in")
    public Mono<Command> allIn(@PathVariable String gameId, @RequestBody AllInRequest request) {
        return gameService.handleCommand(gameId, request.toCommand());
    }

    @PostMapping("/fold")
    public Mono<Command> fold(@PathVariable String gameId, @RequestBody FoldRequest request) {
        return gameService.handleCommand(gameId, request.toCommand());
    }

    @PostMapping("/raise")
    public Mono<Command> raise(@PathVariable String gameId, @RequestBody RaiseRequest request) {
        return gameService.handleCommand(gameId, request.toCommand());
    }

    @PostMapping("/replace")
    public Mono<Command> replace(@PathVariable String gameId, @RequestBody ReplaceRequest request) {
        return gameService.handleCommand(gameId, request.toCommand());
    }
}

package com.molszewski.demos.poker.web.controller;

import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.model.response.NewGameResponse;
import com.molszewski.demos.poker.web.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
@Slf4j
public class GameController {
    private final GameService gameService;

    @PostMapping
    public Mono<ResponseEntity<NewGameResponse>> create() {
        return gameService.createGame()
                .map(ResponseEntity::ok)
                .doOnError(err -> log.error(err.getMessage()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping(value = "{gameId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GameResponse> subscribe(@PathVariable String gameId, @RequestParam String myId) {
        return gameService.subscribeToGameChanges(gameId, myId);
    }

    @GetMapping(value = "{gameId}/exists")
    public Mono<ResponseEntity<Boolean>> exists(@PathVariable String gameId) {
        return gameService.exists(gameId)
                .map(ResponseEntity::ok)
                .doOnError(err -> log.error(err.getMessage()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping(value = "{gameId}")
    public Mono<ResponseEntity<GameResponse>> get(@PathVariable String gameId, @RequestParam String myId) {
        return gameService.getCurrentGame(gameId, myId)
                .map(ResponseEntity::ok)
                .doOnError(err -> log.error(err.getMessage()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}

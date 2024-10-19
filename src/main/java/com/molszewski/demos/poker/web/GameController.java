package com.molszewski.demos.poker.web;

import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.model.response.NewGameResponse;
import com.molszewski.demos.poker.web.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    @PostMapping
    public Mono<ResponseEntity<NewGameResponse>> create() {
        return gameService.createGame()
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping(value = "{gameId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ResponseEntity<GameResponse>> subscribe(@PathVariable String gameId) {
        return gameService.subscribeToGameChanges(gameId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}

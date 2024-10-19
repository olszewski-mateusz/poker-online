package com.molszewski.demos.poker.web;

import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    @PostMapping
    public Mono<String> create() {
        return gameService.createGame();
    }

    @GetMapping(value = "{gameId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GameResponse> subscribe(@PathVariable String gameId) {
        return gameService.subscribe(gameId);
    }
}

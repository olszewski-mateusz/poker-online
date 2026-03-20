package com.molszewski.demos.poker.web.service;

import com.molszewski.demos.poker.engine.ai.AiConfiguration;
import com.molszewski.demos.poker.engine.game.GameEngine;
import com.molszewski.demos.poker.web.model.response.GameResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${poker.ai.host}")
    private String aiHost;

    private final WebClient webClient = WebClient.create();

    public Mono<Void> sendRequestToAi(String gameId, String aiId, AiConfiguration configuration, GameEngine engine) {
        GameResponse gameResponse = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId(gameId)
                        .myId(aiId)
                        .state(engine.getGameState())
                        .configuration(engine.getConfiguration())
                        .build()
        );

        AiRequest body = new AiRequest(configuration, gameResponse);
        return webClient.post()
                .uri(aiHost + "/ai")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .toBodilessEntity()
                .flatMap(response -> {
                  if (response.getStatusCode() != HttpStatus.OK) {
                      return Mono.error(new IllegalStateException("Unexpected response status: " + response.getStatusCode()));
                  }
                  return Mono.empty();
                });
    }
}

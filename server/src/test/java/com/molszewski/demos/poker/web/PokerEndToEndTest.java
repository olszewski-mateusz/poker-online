package com.molszewski.demos.poker.web;

import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.test.EndToEndTest;
import com.molszewski.demos.poker.web.model.request.CheckRequest;
import com.molszewski.demos.poker.web.model.request.JoinRequest;
import com.molszewski.demos.poker.web.model.request.ReadyRequest;
import com.molszewski.demos.poker.web.model.response.ActionResponse;
import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.model.response.NewGameResponse;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@EndToEndTest
class PokerEndToEndTest {
    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @Autowired
    private WebTestClient webClient;

    @Test
    @DisplayName("End to end - simple game from create to drawing phase")
    void simpleStory() {
        assertTrue(redis.isRunning());

        String gameId = webClient.post().uri("/game")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewGameResponse.class)
                .returnResult()
                .getResponseBody().gameId();

        String playerIdA = webClient.post().uri("/game/" + gameId + "/action/join")
                .bodyValue(new JoinRequest("Test A"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .returnResult()
                .getResponseBody().myId();

        String playerIdB = webClient.post().uri("/game/" + gameId + "/action/join")
                .bodyValue(new JoinRequest("Test B"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .returnResult()
                .getResponseBody().myId();

        webClient.post().uri("/game/" + gameId + "/action/ready")
                .bodyValue(new ReadyRequest(playerIdA, true))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        webClient.post().uri("/game/" + gameId + "/action/ready")
                .bodyValue(new ReadyRequest(playerIdB, true))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        GameResponse gameResponse = webClient.get().uri("/game/" + gameId + "?myId=" + playerIdA)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertEquals(GamePhase.FIRST_BETTING, gameResponse.phase());
        assertEquals(gameId, gameResponse.gameId());
        assertEquals(playerIdA, gameResponse.myId());
        assertEquals(2, gameResponse.players().size());

        FluxExchangeResult<String> result = webClient.get().uri("/game/" + gameId + "/subscribe?myId=" + playerIdB)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class);

        StepVerifier.create(result.getResponseBody())
                .expectNextCount(1)
                .then(() -> {
                    webClient.post().uri("/game/" + gameId + "/action/check")
                            .bodyValue(new CheckRequest(playerIdA))
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isOk();
                })
                .then(() -> {
                    webClient.post().uri("/game/" + gameId + "/action/check")
                            .bodyValue(new CheckRequest(playerIdB))
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isOk();
                })
                .expectNextCount(2)
                .verifyTimeout(Duration.ofSeconds(2));

        gameResponse = webClient.get().uri("/game/" + gameId + "?myId=" + playerIdB)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertEquals(GamePhase.DRAWING, gameResponse.phase());
        assertEquals(gameId, gameResponse.gameId());
        assertEquals(playerIdB, gameResponse.myId());

        webClient.get().uri("/game/" + gameId + "/exists")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(response -> assertTrue(response));
    }
}

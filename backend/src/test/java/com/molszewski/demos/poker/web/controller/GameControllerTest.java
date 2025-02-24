package com.molszewski.demos.poker.web.controller;

import com.molszewski.demos.poker.test.IntegrationTest;
import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.model.response.NewGameResponse;
import com.molszewski.demos.poker.web.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@WebFluxTest(GameController.class)
@IntegrationTest
class GameControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private GameService gameService;

    @Test
    @DisplayName("Create game success")
    void createSuccessTest() {
        String gameId = "test";
        when(gameService.createGame()).thenReturn(Mono.just(new NewGameResponse(gameId)));

        webClient.post().uri("/game")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewGameResponse.class)
                .value(response -> assertEquals(gameId, response.gameId()));

        verify(gameService, times(1)).createGame();
    }

    @Test
    @DisplayName("Create game bad request")
    void createBadRequestTest() {
        when(gameService.createGame()).thenReturn(Mono.error(new IllegalArgumentException()));

        webClient.post().uri("/game")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).createGame();
    }

    @Test
    @DisplayName("Subscribe to game success")
    void subscribeSuccessTest() {
        String gameId = "test";
        String myId = "player";
        GameResponse gameResponse1 = GameResponse.builder().gameId(gameId).myId(myId).currentPlayerIndex(0).build();
        GameResponse gameResponse2 = GameResponse.builder().gameId(gameId).myId(myId).currentPlayerIndex(1).build();
        GameResponse gameResponse3 = GameResponse.builder().gameId(gameId).myId(myId).currentPlayerIndex(2).build();

        when(gameService.subscribeToGameChanges(anyString(), anyString()))
                .thenReturn(Flux.just(gameResponse1, gameResponse2, gameResponse3));

        webClient.get().uri("/game/" + gameId + "/subscribe?myId=" + myId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GameResponse.class)
                .hasSize(3)
                .value(gameResponses -> {
                    assertEquals(gameId, gameResponses.get(0).gameId());
                    assertEquals(myId, gameResponses.get(0).myId());
                    assertEquals(0, gameResponses.get(0).currentPlayerIndex());

                    assertEquals(gameId, gameResponses.get(1).gameId());
                    assertEquals(myId, gameResponses.get(1).myId());
                    assertEquals(1, gameResponses.get(1).currentPlayerIndex());

                    assertEquals(gameId, gameResponses.get(2).gameId());
                    assertEquals(myId, gameResponses.get(2).myId());
                    assertEquals(2, gameResponses.get(2).currentPlayerIndex());
                });

        verify(gameService, times(1)).subscribeToGameChanges(gameId, myId);
    }

    @Test
    @DisplayName("Subscribe to game initial error")
    void subscribeErrorTest() {
        when(gameService.subscribeToGameChanges(anyString(), anyString()))
                .thenReturn(Flux.error(new IllegalArgumentException()));

        webClient.get().uri("/game/test/subscribe?myId=test")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBodyList(IllegalArgumentException.class)
                .hasSize(1)
                .value(err -> assertThat(err.get(0)).isInstanceOf(IllegalArgumentException.class));

        verify(gameService, times(1)).subscribeToGameChanges(anyString(), anyString());
    }

    @Test
    @DisplayName("Subscribe to game success then error")
    void subscribeErrorAfterSuccessTest() {
        String gameId = "test";
        String myId = "player";

        Flux<GameResponse> testPublisher = TestPublisher.<GameResponse>createCold()
                .next(GameResponse.builder().gameId(gameId).myId(myId).currentPlayerIndex(0).build())
                .next(GameResponse.builder().gameId(gameId).myId(myId).currentPlayerIndex(1).build())
                .error(new IllegalArgumentException())
                .flux();

        when(gameService.subscribeToGameChanges(anyString(), anyString()))
                .thenReturn(testPublisher);

        FluxExchangeResult<GameResponse> result = webClient.get().uri("/game/test/subscribe?myId=test")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(GameResponse.class);

        StepVerifier.create(result.getResponseBody())
                .assertNext(gameResponse -> assertEquals(0, gameResponse.currentPlayerIndex()))
                .assertNext(gameResponse -> assertEquals(1, gameResponse.currentPlayerIndex()))
                .verifyError(IllegalArgumentException.class);

        verify(gameService, times(1)).subscribeToGameChanges(anyString(), anyString());
    }

    @Test
    @DisplayName("Game exist success")
    void existsSuccessTest() {
        String gameId = "test";

        when(gameService.exists(anyString())).thenReturn(Mono.just(true));

        webClient.get().uri("/game/" + gameId + "/exists")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(response -> assertTrue(response));

        verify(gameService, times(1)).exists(gameId);
    }

    @Test
    @DisplayName("Game exist bad request")
    void existsBadRequestTest() {
        String gameId = "test";

        when(gameService.exists(anyString())).thenReturn(Mono.error(new IllegalArgumentException()));

        webClient.get().uri("/game/" + gameId + "/exists")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).exists(gameId);
    }

    @Test
    @DisplayName("Get game success")
    void getSuccessTest() {
        String gameId = "test";
        String myId = "player";
        GameResponse gameResponse = GameResponse.builder().gameId(gameId).myId(myId).build();
        when(gameService.getCurrentGame(anyString(), anyString())).thenReturn(Mono.just(gameResponse));

        webClient.get().uri("/game/" + gameId + "?myId=" + myId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .value(response -> {
                    assertEquals(gameId, response.gameId());
                    assertEquals(myId, response.myId());
                });

        verify(gameService, times(1)).getCurrentGame(gameId, myId);
    }

    @Test
    @DisplayName("Get game bad request")
    void getBadRequestTest() {
        when(gameService.getCurrentGame(anyString(), anyString())).thenReturn(Mono.error(new IllegalArgumentException()));

        webClient.get().uri("/game/test?myId=test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).getCurrentGame(anyString(), anyString());
    }
}
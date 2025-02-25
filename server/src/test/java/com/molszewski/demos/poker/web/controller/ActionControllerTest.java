package com.molszewski.demos.poker.web.controller;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.persistence.entity.command.*;
import com.molszewski.demos.poker.test.IntegrationTest;
import com.molszewski.demos.poker.web.model.request.*;
import com.molszewski.demos.poker.web.model.response.ActionResponse;
import com.molszewski.demos.poker.web.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest(ActionController.class)
@IntegrationTest
class ActionControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private GameService gameService;

    @Test
    @DisplayName("Join action success")
    void joinSuccessTest() {
        String myId = "player";
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.just(new ActionResponse(myId)));
        when(gameService.generatePlayerId()).thenReturn(playerId);

        String displayName = "display-name";
        JoinRequest joinRequest = new JoinRequest(displayName);
        webClient.post().uri("/game/test/action/join")
                .bodyValue(joinRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .value(actionResponse -> {
                    assertEquals(myId, actionResponse.myId());
                });

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof JoinCommand joinCommand &&
                        joinCommand.getPlayerId().equals(playerId) &&
                        joinCommand.getDisplayName().equals(displayName))
        );
    }

    @Test
    @DisplayName("Join action bad request")
    void joinBadRequestTest() {
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.error(new IllegalArgumentException()));
        when(gameService.generatePlayerId()).thenReturn(playerId);

        String displayName = "display-name";
        JoinRequest joinRequest = new JoinRequest(displayName);
        webClient.post().uri("/game/test/action/join")
                .bodyValue(joinRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof JoinCommand joinCommand &&
                        joinCommand.getPlayerId().equals(playerId) &&
                        joinCommand.getDisplayName().equals(displayName))
        );
    }

    @Test
    @DisplayName("Ready action success")
    void readySuccessTest() {
        String myId = "player";
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.just(new ActionResponse(myId)));

        ReadyRequest readyRequest = new ReadyRequest(playerId, true);
        webClient.post().uri("/game/test/action/ready")
                .bodyValue(readyRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .value(actionResponse -> {
                    assertEquals(myId, actionResponse.myId());
                });

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof ReadyCommand readyCommand &&
                        readyCommand.getPlayerId().equals(playerId) &&
                        readyCommand.isReady())
        );
    }

    @Test
    @DisplayName("Ready action bad request")
    void readyBadRequestTest() {
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.error(new IllegalArgumentException()));

        ReadyRequest readyRequest = new ReadyRequest(playerId, true);
        webClient.post().uri("/game/test/action/ready")
                .bodyValue(readyRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof ReadyCommand readyCommand &&
                        readyCommand.getPlayerId().equals(playerId) &&
                        readyCommand.isReady())
        );
    }

    @Test
    @DisplayName("Check action success")
    void checkSuccessTest() {
        String myId = "player";
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.just(new ActionResponse(myId)));

        CheckRequest checkRequest = new CheckRequest(playerId);
        webClient.post().uri("/game/test/action/check")
                .bodyValue(checkRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .value(actionResponse -> {
                    assertEquals(myId, actionResponse.myId());
                });

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof CheckCommand checkCommand &&
                        checkCommand.getPlayerId().equals(playerId)
        ));
    }

    @Test
    @DisplayName("Check action bad request")
    void checkBadRequestTest() {
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.error(new IllegalArgumentException()));

        CheckRequest checkRequest = new CheckRequest(playerId);
        webClient.post().uri("/game/test/action/check")
                .bodyValue(checkRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof CheckCommand checkCommand &&
                        checkCommand.getPlayerId().equals(playerId)
        ));
    }

    @Test
    @DisplayName("All in action success")
    void allInSuccessTest() {
        String myId = "player";
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.just(new ActionResponse(myId)));

        AllInRequest allInRequest = new AllInRequest(playerId);
        webClient.post().uri("/game/test/action/all-in")
                .bodyValue(allInRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .value(actionResponse -> {
                    assertEquals(myId, actionResponse.myId());
                });

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof AllInCommand allInCommand &&
                        allInCommand.getPlayerId().equals(playerId)
        ));
    }

    @Test
    @DisplayName("All in action bad request")
    void allInBadRequestTest() {
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.error(new IllegalArgumentException()));

        AllInRequest allInRequest = new AllInRequest(playerId);
        webClient.post().uri("/game/test/action/all-in")
                .bodyValue(allInRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof AllInCommand allInCommand &&
                        allInCommand.getPlayerId().equals(playerId)
        ));
    }

    @Test
    @DisplayName("Fold action success")
    void foldSuccessTest() {
        String myId = "player";
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.just(new ActionResponse(myId)));

        FoldRequest foldRequest = new FoldRequest(playerId);
        webClient.post().uri("/game/test/action/fold")
                .bodyValue(foldRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .value(actionResponse -> {
                    assertEquals(myId, actionResponse.myId());
                });

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof FoldCommand foldCommand &&
                        foldCommand.getPlayerId().equals(playerId)
        ));
    }

    @Test
    @DisplayName("Fold action bad request")
    void foldBadRequestTest() {
        String playerId = "player-id";
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.error(new IllegalArgumentException()));

        FoldRequest foldRequest = new FoldRequest(playerId);
        webClient.post().uri("/game/test/action/fold")
                .bodyValue(foldRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof FoldCommand foldCommand &&
                        foldCommand.getPlayerId().equals(playerId)
        ));
    }

    @Test
    @DisplayName("Raise action success")
    void raiseSuccessTest() {
        String myId = "player";
        String playerId = "player-id";
        int amount = 100;
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.just(new ActionResponse(myId)));

        RaiseRequest raiseRequest = new RaiseRequest(playerId, amount);
        webClient.post().uri("/game/test/action/raise")
                .bodyValue(raiseRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .value(actionResponse -> {
                    assertEquals(myId, actionResponse.myId());
                });

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof RaiseCommand raiseCommand &&
                        raiseCommand.getPlayerId().equals(playerId) &&
                        raiseCommand.getAmount() == amount)
        );
    }

    @Test
    @DisplayName("Raise action bad request")
    void raiseBadRequestTest() {
        String playerId = "player-id";
        int amount = 100;
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.error(new IllegalArgumentException()));

        RaiseRequest raiseRequest = new RaiseRequest(playerId, amount);
        webClient.post().uri("/game/test/action/raise")
                .bodyValue(raiseRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof RaiseCommand raiseCommand &&
                        raiseCommand.getPlayerId().equals(playerId) &&
                        raiseCommand.getAmount() == amount)
        );
    }

    @Test
    @DisplayName("Replace action success")
    void replaceSuccessTest() {
        String myId = "player";
        String playerId = "player-id";
        Set<Card> cards = Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS)
        );
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.just(new ActionResponse(myId)));

        ReplaceRequest replaceRequest = new ReplaceRequest(playerId, cards);
        webClient.post().uri("/game/test/action/replace")
                .bodyValue(replaceRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ActionResponse.class)
                .value(actionResponse -> {
                    assertEquals(myId, actionResponse.myId());
                });

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof ReplaceCommand replaceCommand &&
                        replaceCommand.getPlayerId().equals(playerId) &&
                        replaceCommand.getCardsToReplace().containsAll(cards)
        ));
    }

    @Test
    @DisplayName("Replace action bad request")
    void replaceBadRequestTest() {
        String playerId = "player-id";
        Set<Card> cards = Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS)
        );
        when(gameService.handleCommand(eq("test"), any())).thenReturn(Mono.error(new IllegalArgumentException()));

        ReplaceRequest replaceRequest = new ReplaceRequest(playerId, cards);
        webClient.post().uri("/game/test/action/replace")
                .bodyValue(replaceRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(gameService, times(1)).handleCommand(eq("test"), argThat(
                command -> command instanceof ReplaceCommand replaceCommand &&
                        replaceCommand.getPlayerId().equals(playerId) &&
                        replaceCommand.getCardsToReplace().containsAll(cards)
        ));
    }
}
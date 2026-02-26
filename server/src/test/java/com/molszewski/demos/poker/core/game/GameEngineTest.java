package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Join;
import com.molszewski.demos.poker.core.game.state.exception.GameStateException;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
class GameEngineTest {

    @Mock
    private GameState gameState;

    @Mock
    private GameConfiguration configuration;

    @Test
    void getCurrentPlayerTest() {
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        gameEngine.getCurrentPlayer();
        verify(gameState, times(1)).getCurrentPlayer();
    }

    @Test
    void applyActionTest() throws GameStateException {
        Action action = mock(Join.class);
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        boolean success = gameEngine.applyAction(action);
        verify(action, times(1)).apply(eq(gameState), eq(configuration));
        assertTrue(success);
    }

    @Test
    void applyActionThrows() throws GameStateException {
        Action action = mock(Join.class);
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        doThrow(new GameStateException("")).when(action).apply(any(), any());

        boolean success = gameEngine.applyAction(action);
        verify(action, times(1)).apply(eq(gameState), eq(configuration));
        assertFalse(success);
    }

    @Test
    void getPhaseTest() {
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        when(gameState.getGamePhase()).thenReturn(GamePhase.SECOND_BETTING);

        GamePhase gamePhase = gameEngine.getPhase();

        verify(gameState, times(1)).getGamePhase();
        assertEquals(GamePhase.SECOND_BETTING, gamePhase);
    }

    @Test
    void getPlayersTest() {
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        List<Player> players = List.of(
                new Player("1", 0),
                new Player("2", 0)
        );

        when(gameState.getPlayers()).thenReturn(players);

        List<Player> returnedPlayers = gameEngine.getPlayers();

        verify(gameState, times(1)).getPlayers();
        assertNotSame(players.get(0), returnedPlayers.get(0));
        assertNotSame(players.get(1), returnedPlayers.get(1));
        assertEquals(players.get(0).getId(), returnedPlayers.get(0).getId());
        assertEquals(players.get(1).getId(), returnedPlayers.get(1).getId());
    }

    @Test
    void getCardsTest() {
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        List<Card> cards = List.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS)
        );

        when(gameState.getDeck()).thenReturn(new Deck(cards, List.of(), null));

        int amount = gameEngine.getCardsInDeck();

        verify(gameState, times(1)).getDeck();
        assertEquals(cards.size(), amount);
    }

    @Test
    void getDiscardsTest() {
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        List<Card> cards = List.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS)
        );

        when(gameState.getDeck()).thenReturn(new Deck(List.of(), cards, null));

        int amount = gameEngine.getDiscardedCards();

        verify(gameState, times(1)).getDeck();
        assertEquals(cards.size(), amount);
    }
}

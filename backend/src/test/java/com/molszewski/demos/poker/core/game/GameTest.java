package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Join;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameTest {

    @Mock
    private GameState gameState;

    @Mock
    private StateManager stateManager;

    @InjectMocks
    private Game game;

    @Test
    void getCurrentPlayerTest() {
        game.getCurrentPlayer();
        verify(gameState, times(1)).getCurrentPlayer();
    }

    @Test
    void applyActionTest() throws ActionException {
        Action action = new Join("1");
        boolean success = game.applyAction(action);
        verify(stateManager, times(1)).executeAction(eq(action), eq(gameState), any());
        assertTrue(success);
    }

    @Test
    void applyActionThrows() throws ActionException {
        doThrow(new ActionException("")).when(stateManager).executeAction(any(), any(), any());

        Action action = new Join("1");
        boolean success = game.applyAction(action);
        verify(stateManager, times(1)).executeAction(eq(action), eq(gameState), any());
        assertFalse(success);
    }

    @Test
    void getPhaseTest() {
        when(gameState.getGamePhase()).thenReturn(GamePhase.SECOND_BETTING);

        GamePhase gamePhase = game.getPhase();

        verify(gameState, times(1)).getGamePhase();
        assertEquals(GamePhase.SECOND_BETTING, gamePhase);
    }

    @Test
    void getPlayersTest() {
        List<Player> players = List.of(
                new Player("1", 0),
                new Player("2", 0)
        );

        when(gameState.getPlayers()).thenReturn(players);

        List<Player> returnedPlayers = game.getPlayers();

        verify(gameState, times(1)).getPlayers();
        assertNotSame(players.get(0), returnedPlayers.get(0));
        assertNotSame(players.get(1), returnedPlayers.get(1));
        assertEquals(players.get(0).getId(), returnedPlayers.get(0).getId());
        assertEquals(players.get(1).getId(), returnedPlayers.get(1).getId());
    }

    @Test
    void getCardsTest() {
        List<Card> cards = List.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS)
        );

        when(gameState.getDeck()).thenReturn(new Deck(cards, List.of(), null));

        int amount = game.getCardsInDeck();

        verify(gameState, times(1)).getDeck();
        assertEquals(cards.size(), amount);
    }

    @Test
    void getDiscardsTest() {
        List<Card> cards = List.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS)
        );

        when(gameState.getDeck()).thenReturn(new Deck(List.of(), cards, null));

        int amount = game.getDiscardedCards();

        verify(gameState, times(1)).getDeck();
        assertEquals(cards.size(), amount);
    }
}
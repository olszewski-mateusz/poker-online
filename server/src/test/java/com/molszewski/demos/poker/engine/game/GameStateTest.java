package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.history.PhaseChangeEntry;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class GameStateTest {

    @Test
    void getPlayerByIdAndExistsTest() {
        Player a = new Player("A", "A", 100);
        Player b = new Player("B", "B", 100);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(a, b));

        assertTrue(gameState.playerExists("B"));
        assertSame(b, gameState.getPlayerById("B"));
        assertThrows(IllegalStateException.class, () -> gameState.getPlayerById("missing"));
    }

    @Test
    void getPlayerIndexTest() {
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(
                new Player("A", "A", 100),
                new Player("B", "B", 100),
                new Player("C", "C", 100)
        ));

        assertEquals(1, gameState.getPlayerIndex("B"));
        assertThrows(IllegalStateException.class, () -> gameState.getPlayerIndex("D"));
    }

    @Test
    void validatePhaseTest() {
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of());
        gameState.setGamePhase(GamePhase.SECOND_BETTING);

        assertDoesNotThrow(() -> gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING)));
        assertThrows(ActionValidationException.class, () -> gameState.validatePhase(Set.of(GamePhase.DRAWING)));
    }

    @Test
    void validateCurrentPlayerTest() {
        Player p1 = new Player("1", "1", 100, false, false, null);
        Player p2 = new Player("2", "2", 100, false, false, null);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(p1, p2));

        gameState.setCurrentPlayer(p2);
        assertDoesNotThrow(() -> gameState.validateCurrentPlayer("2"));
        assertThrows(ActionValidationException.class, () -> gameState.validateCurrentPlayer("1"));

        p2.setReady(true);
        assertThrows(ActionValidationException.class, () -> gameState.validateCurrentPlayer("2"));
    }

    @Test
    void setGamePhaseAddsHistoryEntry() {
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of());

        gameState.setGamePhase(GamePhase.FIRST_BETTING);

        assertEquals(1, gameState.getHistory().getEntries().size());
        assertInstanceOf(PhaseChangeEntry.class, gameState.getHistory().getEntries().getFirst());
        assertEquals(GamePhase.FIRST_BETTING, ((PhaseChangeEntry) gameState.getHistory().getEntries().getFirst()).getPhase());
    }
}

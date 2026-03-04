package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import com.molszewski.demos.poker.test.TestPlayerState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class GameStateTest {

    @Test
    void getPlayerByIdAndExistsTest() {
        Player a = new Player("A", "A", 100, false, true, null);
        Player b = new Player("B", "B", 100, false, true, null);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(a, b));

        assertTrue(gameState.getPlayerState().exists("B"));
        assertSame(b, gameState.getPlayerState().getById("B"));
        assertThrows(IllegalStateException.class, () -> gameState.getPlayerState().getById("missing"));
    }

    @Test
    void getPlayerIndexTest() {
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(
                new Player("A", "A", 100, false, true, null),
                new Player("B", "B", 100, false, true, null),
                new Player("C", "C", 100, false, true, null)
        ));

        assertEquals(1, gameState.getPlayerState().getIndex("B"));
        assertThrows(IllegalStateException.class, () -> gameState.getPlayerState().getIndex("D"));
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

        TestPlayerState.setCurrentPlayer(gameState, p2);
        assertDoesNotThrow(() -> gameState.getPlayerState().canPlayerMakeAction("2"));
        assertThrows(ActionValidationException.class, () -> gameState.getPlayerState().canPlayerMakeAction("1"));

        p2.setReady(true);
        assertThrows(ActionValidationException.class, () -> gameState.getPlayerState().canPlayerMakeAction("2"));
    }

    @Test
    void setGamePhaseAddsHistoryEntry() {
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of());

        gameState.setGamePhase(GamePhase.FIRST_BETTING);

        assertEquals(1, gameState.getHistory().getEntries().size());
        assertEquals("PHASE_CHANGE", gameState.getHistory().getEntries().getFirst().getEntryCategory());
        assertEquals(GamePhase.FIRST_BETTING.toString(), gameState.getHistory().getEntries().getFirst().getProperties().get("newPhase"));
    }
}

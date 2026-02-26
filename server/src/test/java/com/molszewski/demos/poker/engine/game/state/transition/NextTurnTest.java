package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class NextTurnTest {

    @Test
    @DisplayName("Next turn without new phase from betting phase")
    void nextTurnNoNewPhaseBettingPhaseTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, false, true, null),
                new Player("2", "2", 0, 100, false, false, null),
                new Player("3", "3", 100, 100, false, false, null),
                new Player("4", "4", 100, 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("4"));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);

        ActionFlow.nextTurn(gameState);

        assertEquals("3", gameState.getCurrentPlayer().getId());
    }

    @Test
    @DisplayName("Next turn without new phase from drawing phase")
    void nextTurnNoNewPhaseDrawingPhaseTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, false, true, null),
                new Player("2", "2", 0, 100, false, false, null),
                new Player("3", "3", 100, 100, false, false, null),
                new Player("4", "4", 100, 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("4"));
        gameState.setGamePhase(GamePhase.DRAWING);

        ActionFlow.nextTurn(gameState);

        assertEquals("2", gameState.getCurrentPlayer().getId());
    }

    @Test
    @DisplayName("Next turn without new phase no players throws")
    void nextTurnNoNewPhaseNoPlayersThrowsTest() {
        GameState gameState = new GameState(null, List.of());
        assertThrows(IllegalStateException.class, () -> ActionFlow.nextTurn(gameState));
    }

    @Test
    @DisplayName("Next turn without new phase no current player throws")
    void nextTurnNoNewPhaseNoCurrentPlayerThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100),
                new Player("2", "2", 0)
        ));

        assertThrows(IllegalStateException.class, () -> ActionFlow.nextTurn(gameState));
    }

    @Test
    @DisplayName("Next turn with new phase from first betting phase")
    void nextTurnNewPhaseFromFirstBettingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 100, 100, true, false, null),
                new Player("3", "3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.DRAWING, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
    }

    @Test
    @DisplayName("Next turn with new phase from drawing phase")
    void nextTurnNewPhaseFromDrawingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 0, 100, true, false, null),
                new Player("3", "3", 100, 100, false, true, null),
                new Player("4", "4", 100, 100, true, false, null)
        ));
        gameState.setGamePhase(GamePhase.DRAWING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SECOND_BETTING, gameState.getGamePhase());
        assertFalse(gameState.getPlayerById("1").isReady());
        assertTrue(gameState.getPlayerById("2").isReady());
        assertFalse(gameState.getPlayerById("3").isReady());
        assertFalse(gameState.getPlayerById("4").isReady());
    }

    @Test
    @DisplayName("Next turn with new phase from second betting phase")
    void nextTurnNewPhaseFromSecondBettingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 0, 100, true, false, null),
                new Player("3", "3", 100, 100, false, true, null),
                new Player("4", "4", 100, 100, true, false, null)
        ));
        gameState.setGamePhase(GamePhase.SECOND_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("Next turn from unexpected phase throws")
    void nextTurnNewPhaseFromUnexpectedThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 100, 100, true, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));
        gameState.setGamePhase(GamePhase.FINISHED);

        assertThrows(IllegalStateException.class, () -> ActionFlow.nextTurn(gameState));
    }

    @Test
    @DisplayName("Next turn from first betting phase skip to showdown")
    void nextTurnNewPhaseFromBettingAllFoldedTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 0, 100, true, true, null),
                new Player("3", "3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("Next turn from drawing phase skip to showdown")
    void nextTurnNewPhaseFromDrawingAllFoldedTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 0, 100, true, false, null),
                new Player("3", "3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.DRAWING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }
}


package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NextTurnTest {

    private final GameConfiguration gameConfiguration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Next turn without new phase from betting phase")
    void nextTurnNoNewPhaseBettingPhaseTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, false, true, null),
                new Player("2", 0, 100, false, false, null),
                new Player("3", 100, 100, false, false, null),
                new Player("4", 100, 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("4").get());
        gameState.setGamePhase(GamePhase.FIRST_BETTING);

        NextTurn transition = NextTurn.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals("3", gameState.getCurrentPlayer().getId());
    }

    @Test
    @DisplayName("Next turn without new phase from drawing phase")
    void nextTurnNoNewPhaseDrawingPhaseTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, false, true, null),
                new Player("2", 0, 100, false, false, null),
                new Player("3", 100, 100, false, false, null),
                new Player("4", 100, 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("4").get());
        gameState.setGamePhase(GamePhase.DRAWING);

        NextTurn transition = NextTurn.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals("2", gameState.getCurrentPlayer().getId());
    }

    @Test
    @DisplayName("Next turn without new phase no players throws")
    void nextTurnNoNewPhaseNoPlayersThrowsTest() {
        GameState gameState = new GameState(null, List.of());

        NextTurn transition = NextTurn.of();

        assertThrows(IllegalStateException.class, () -> transition.apply(gameState, gameConfiguration));
    }

    @Test
    @DisplayName("Next turn without new phase no current player throws")
    void nextTurnNoNewPhaseNoCurrentPlayerThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100),
                new Player("2", 0)
        ));

        NextTurn transition = NextTurn.of();

        assertThrows(IllegalStateException.class, () -> transition.apply(gameState, gameConfiguration));
    }

    @Test
    @DisplayName("Next turn with new phase from first betting phase")
    void nextTurnNewPhaseFromFirstBettingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, true, false, null),
                new Player("2", 100, 100, true, false, null),
                new Player("3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        NextTurn transition = NextTurn.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.DRAWING, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
    }

    @Test
    @DisplayName("Next turn with new phase from drawing phase")
    void nextTurnNewPhaseFromDrawingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, true, false, null),
                new Player("2", 0, 100, true, false, null),
                new Player("3", 100, 100, false, true, null),
                new Player("4", 100, 100, true, false, null)
        ));
        gameState.setGamePhase(GamePhase.DRAWING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        NextTurn transition = NextTurn.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.SECOND_BETTING, gameState.getGamePhase());
        assertFalse(gameState.getPlayerById("1").get().isReady());
        assertTrue(gameState.getPlayerById("2").get().isReady());
        assertFalse(gameState.getPlayerById("3").get().isReady());
        assertFalse(gameState.getPlayerById("3").get().isReady());
    }

    @Test
    @DisplayName("Next turn with new phase from second betting phase")
    void nextTurnNewPhaseFromSecondBettingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, true, false, null),
                new Player("2", 0, 100, true, false, null),
                new Player("3", 100, 100, false, true, null),
                new Player("4", 100, 100, true, false, null)
        ));
        gameState.setGamePhase(GamePhase.SECOND_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        NextTurn transition = NextTurn.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("Next turn from unexpected phase throws")
    void nextTurnNewPhaseFromUnexpectedThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, true, false, null),
                new Player("2", 100, 100, true, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        gameState.setGamePhase(GamePhase.FINISHED);
        NextTurn transition = NextTurn.of();

        assertThrows(IllegalStateException.class, () -> transition.apply(gameState, gameConfiguration));
    }

    @Test
    @DisplayName("Next turn from first betting phase skip to showdown")
    void nextTurnNewPhaseFromBettingAllFoldedTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, true, false, null),
                new Player("2", 0, 100, true, true, null),
                new Player("3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        NextTurn transition = NextTurn.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("Next turn from drawing phase skip to showdown")
    void nextTurnNewPhaseFromDrawingAllFoldedTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, true, false, null),
                new Player("2", 0, 100, true, false, null),
                new Player("3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.DRAWING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        NextTurn transition = NextTurn.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }
}
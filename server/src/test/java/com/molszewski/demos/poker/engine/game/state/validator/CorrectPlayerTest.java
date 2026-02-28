package com.molszewski.demos.poker.engine.game.state.validator;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
class CorrectPlayerTest {

    @Test
    @DisplayName("Correct player validation")
    void validateTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, false, false, null),
                new Player("2", "2", 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2"));

        assertDoesNotThrow(() -> gameState.validateCurrentPlayer("2"));
    }

    @Test
    @DisplayName("Already ready player validation")
    void validateAlreadyReadyThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, false, false, null),
                new Player("2", "2", 100, true, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2"));

        assertThrows(ActionValidationException.class, () -> gameState.validateCurrentPlayer("2"));
    }

    @Test
    @DisplayName("Folded player validation")
    void validateFoldedThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, false, false, null),
                new Player("2", "2", 100, false, true, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2"));

        assertThrows(ActionValidationException.class, () -> gameState.validateCurrentPlayer("2"));
    }

    @Test
    @DisplayName("Not player's turn validation")
    void validateNotTurnTestTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, false, false, null),
                new Player("2", "2", 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2"));

        assertThrows(ActionValidationException.class, () -> gameState.validateCurrentPlayer("1"));
    }

    @Test
    @DisplayName("No current player validation")
    void validateNoCurrentPlayerThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, false, false, null),
                new Player("2", "2", 100, false, false, null)
        ));

        assertThrows(ActionValidationException.class, () -> gameState.validateCurrentPlayer("1"));
    }

}

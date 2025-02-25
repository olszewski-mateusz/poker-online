package com.molszewski.demos.poker.core.game.state.validator;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Check;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class CorrectPlayerTest {

    @Test
    @DisplayName("Correct player validation")
    void validateTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100 ,100, false, false, null),
                new Player("2", 100 ,100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2").get());

        Action action = new Check("2");
        CorrectPlayer validator = CorrectPlayer.of();
        assertDoesNotThrow(() -> validator.validate(action, gameState));
    }

    @Test
    @DisplayName("Already ready player validation")
    void validateAlreadyReadyThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100 ,100, false, false, null),
                new Player("2", 100 ,100, true, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2").get());

        Action action = new Check("2");
        CorrectPlayer validator = CorrectPlayer.of();
        assertThrows(ActionException.class, () -> validator.validate(action, gameState));
    }

    @Test
    @DisplayName("Folded player validation")
    void validateFoldedThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100 ,100, false, false, null),
                new Player("2", 100 ,100, false, true, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2").get());

        Action action = new Check("2");
        CorrectPlayer validator = CorrectPlayer.of();
        assertThrows(ActionException.class, () -> validator.validate(action, gameState));
    }

    @Test
    @DisplayName("Not player's turn validation")
    void validateNotTurnTestTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100 ,100, false, false, null),
                new Player("2", 100 ,100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("2").get());

        Action action = new Check("1");
        CorrectPlayer validator = CorrectPlayer.of();
        assertThrows(ActionException.class, () -> validator.validate(action, gameState));
    }

}
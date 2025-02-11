package com.molszewski.demos.poker.core.game.state.validator;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Check;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorrectPhaseTest {

    @Test
    @DisplayName("Correct phase")
    void correctPhaseTest() {
        GameState gameState = new GameState(null, List.of());
        gameState.setGamePhase(GamePhase.SECOND_BETTING);

        Action action = new Check("2");
        CorrectPhase validator = CorrectPhase.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING);
        assertDoesNotThrow(() -> validator.validate(action, gameState));
    }

    @Test
    @DisplayName("Not correct phase")
    void correctPhaseThrowsTest() {
        GameState gameState = new GameState(null, List.of());
        gameState.setGamePhase(GamePhase.SECOND_BETTING);

        Action action = new Check("2");
        CorrectPhase validator = CorrectPhase.of(GamePhase.FIRST_BETTING);
        assertThrows(ActionException.class, () -> validator.validate(action, gameState));
    }
}
package com.molszewski.demos.poker.engine.game.state.validator;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
class CorrectPhaseTest {

    @Test
    @DisplayName("Correct phase")
    void correctPhaseTest() {
        GameState gameState = new GameState(null, List.of());
        gameState.setGamePhase(GamePhase.SECOND_BETTING);

        assertDoesNotThrow(() -> gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING)));
    }

    @Test
    @DisplayName("Not correct phase")
    void correctPhaseThrowsTest() {
        GameState gameState = new GameState(null, List.of());
        gameState.setGamePhase(GamePhase.SECOND_BETTING);

        assertThrows(ActionValidationException.class, () -> gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING)));
    }
}

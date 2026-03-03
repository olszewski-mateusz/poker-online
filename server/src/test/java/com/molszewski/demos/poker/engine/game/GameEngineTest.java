package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void applyActionTest() throws ActionValidationException {
        Action action = mock(Join.class);
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        gameEngine.tryApplyAction(action);
        verify(action, times(1)).apply(eq(gameState), eq(configuration));
    }

    @Test
    void applyActionThrows() throws ActionValidationException {
        Action action = mock(Join.class);
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        doThrow(new ActionValidationException("")).when(action).apply(any(), any());

        assertThrows(ActionValidationException.class, () -> gameEngine.tryApplyAction(action));
        verify(action, times(1)).apply(eq(gameState), eq(configuration));
    }

    @Test
    void getterTest() {
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        assertSame(gameState, gameEngine.getGameState());
        assertSame(configuration, gameEngine.getConfiguration());
    }
}

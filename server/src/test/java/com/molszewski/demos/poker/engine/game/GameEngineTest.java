package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.action.Join;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
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
    void applyActionTest() throws GameStateException {
        Action action = mock(Join.class);
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        boolean success = gameEngine.applyAction(action);
        verify(action, times(1)).apply(eq(gameState), eq(configuration));
        assertTrue(success);
    }

    @Test
    void applyActionThrows() throws GameStateException {
        Action action = mock(Join.class);
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        doThrow(new GameStateException("")).when(action).apply(any(), any());

        boolean success = gameEngine.applyAction(action);
        verify(action, times(1)).apply(eq(gameState), eq(configuration));
        assertFalse(success);
    }

    @Test
    void getterTest() {
        GameEngine gameEngine = new GameEngine(gameState, configuration);
        assertSame(gameState, gameEngine.getGameState());
        assertSame(configuration, gameEngine.getConfiguration());
    }
}


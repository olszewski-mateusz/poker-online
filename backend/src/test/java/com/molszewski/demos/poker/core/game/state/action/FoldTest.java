package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FoldTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void simpleRaise() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);
        stateManager.executeAction(new Ready("3",true), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        stateManager.executeAction(new Fold("1"), gameState, configuration);
        assertTrue(player.isReady());
        assertTrue(player.isFolded());
    }
}
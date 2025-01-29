package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class JoinTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("One player joins")
    void onePlayerCorrectlyAdded() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);

        assertEquals(1, gameState.getPlayers().size());
        Player player = gameState.getPlayers().getFirst();
        assertEquals(configuration.startMoney(), player.getMoney());
        assertNull(player.getHand());
        assertEquals(0, player.getBet());
        assertEquals("1", player.getId());
        assertFalse(player.isReady());
        assertFalse(player.isFolded());
    }

    @Test
    @DisplayName("Three players join")
    void threePlayerCorrectlyAdded() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        assertEquals(3, gameState.getPlayers().size());
    }

    @Test
    @DisplayName("One player added two times - throws error")
    void samePlayerAddedTwoTimes() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        assertThrows(ActionException.class,
                () -> stateManager.executeAction(new Join("1"), gameState, configuration));
    }
}
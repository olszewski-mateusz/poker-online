package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ReadyTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void onePlayerReady() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("2", true), gameState, configuration);

        assertEquals(1, gameState.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    void morePlayerReady() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);
        stateManager.executeAction(new Join("4"), gameState, configuration);

        stateManager.executeAction(new Ready("4", true), gameState, configuration);
        stateManager.executeAction(new Ready("1", true), gameState, configuration);
        stateManager.executeAction(new Ready("3", true), gameState, configuration);

        assertEquals(3, gameState.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    void playerReadyTwoTimes() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("2", true), gameState, configuration);
        assertThrows(ActionException.class,
                () -> stateManager.executeAction(new Ready("2", true), gameState, configuration));
    }

    @Test
    void playerNotReady() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        assertThrows(ActionException.class,
                () -> stateManager.executeAction(new Ready("2", false), gameState, configuration));
    }

    @Test
    void playerReadyThenNotReady() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("2", true), gameState, configuration);
        stateManager.executeAction(new Ready("2", false), gameState, configuration);

        assertFalse(gameState.getPlayerById("2").get().isReady());
    }
}
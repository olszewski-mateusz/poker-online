package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class CheckTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly checks")
    void simpleCheck() throws ActionException {
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
        stateManager.executeAction(new Check("1"), gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(configuration.ante(), player.getBet());
        assertEquals(configuration.startMoney() - configuration.ante(), player.getMoney());
    }

    @Test
    @DisplayName("Player correctly calls")
    void simpleCall() throws ActionException {
        int raiseMoney = 3 * configuration.ante();

        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("3",true), gameState, configuration);
        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);

        stateManager.executeAction(new Raise("1", raiseMoney), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("2", player.getId());
        stateManager.executeAction(new Check("2"), gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(raiseMoney, player.getBet());
        assertEquals(configuration.startMoney() - raiseMoney, player.getMoney());
    }

    @Test
    @DisplayName("Non existing player checks - throws error")
    void playerNotFound() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("3",true), gameState, configuration);
        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);

        assertThrows(ActionException.class, () -> stateManager.executeAction(new Check("4"), gameState, configuration));
    }

    @Test
    @DisplayName("Player calls when he doesn't have enough chips - throws error")
    void checkNoMoney() throws ActionException {
        int raiseMoney = 2 * configuration.startMoney();

        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("3",true), gameState, configuration);
        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);

        gameState.getCurrentPlayer().addMoney(raiseMoney);
        stateManager.executeAction(new Raise("1", raiseMoney), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("2", player.getId());
        assertEquals(raiseMoney, gameState.getCurrentBet());
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Check("2"), gameState, configuration));
    }
}
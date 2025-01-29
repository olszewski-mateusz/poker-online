package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AllInTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly goes all in")
    void simpleAllIn() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("1", true), gameState, configuration);
        stateManager.executeAction(new Ready("2", true), gameState, configuration);
        stateManager.executeAction(new Ready("3", true), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        stateManager.executeAction(new AllIn("1"), gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(configuration.startMoney(), player.getBet());
        assertEquals(0, player.getMoney());
    }

    @Test
    @DisplayName("When player goes all in - others are still ready (when they don't have chips)")
    void allInNotChangingReadyZeroChips() throws ActionException {
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
        stateManager.executeAction(new Raise("1", 2 * configuration.ante()), gameState, configuration);
        assertTrue(player.isReady());
        player.transferMoneyToBet(player.getMoney());
        player.clearAndGetBet();
        stateManager.executeAction(new AllIn("2"), gameState, configuration);
        assertTrue(player.isReady());
    }

    @Test
    @DisplayName("When player goes all in - others are still ready (when current bet isn't changed)")
    void allInNotChangingReadyNoNewMaxBet() throws ActionException {
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
        stateManager.executeAction(new Raise("1", configuration.startMoney()), gameState, configuration);
        player.addMoney(100);
        assertTrue(player.isReady());
        stateManager.executeAction(new AllIn("2"), gameState, configuration);
        assertTrue(player.isReady());
    }

    @Test
    @DisplayName("When player goes all in - others are not ready (when they have chips)")
    void allInChangingReady() throws ActionException {
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
        stateManager.executeAction(new AllIn("2"), gameState, configuration);
        assertFalse(player.isReady());
    }
}
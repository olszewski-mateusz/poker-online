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
class RaiseTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly bets 100 chips")
    void simpleBet() throws ActionException {
        int raiseMoney = 100;

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
        stateManager.executeAction(new Raise("1", raiseMoney), gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(raiseMoney, player.getBet());
        assertEquals(configuration.startMoney() - raiseMoney, player.getMoney());
    }

    @Test
    @DisplayName("Player bets too little chips - throws error")
    void raiseTooLittle() throws ActionException {
        int raiseMoney = configuration.ante() + 1;

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
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Raise("1", raiseMoney), gameState, configuration));
    }

    @Test
    @DisplayName("Player bets too many chips - throws error")
    void raiseTooMany() throws ActionException {
        int raiseMoney = configuration.startMoney() + 1;

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
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Raise("1", raiseMoney), gameState, configuration));
    }

    @Test
    @DisplayName("When player raises - others are not ready (when they have chips)")
    void raiseChangesReadyStatus() throws ActionException {
        int raiseMoney = 2 * configuration.ante();

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
        assertFalse(player.isReady());
        stateManager.executeAction(new Check("1"), gameState, configuration);
        assertTrue(player.isReady());
        stateManager.executeAction(new Raise("2", raiseMoney), gameState, configuration);
        assertFalse(player.isReady());
    }

    @Test
    @DisplayName("When player raises - others are still ready (when they don't have chips)")
    void raiseNotChangesReadyStatus() throws ActionException {
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
        assertFalse(player.isReady());
        stateManager.executeAction(new Check("1"), gameState, configuration);
        assertTrue(player.isReady());

        player.transferMoneyToBet(player.getMoney());
        player.clearAndGetBet();
        stateManager.executeAction(new Raise("2", configuration.startMoney()), gameState, configuration);
        assertTrue(player.isReady());
    }
}
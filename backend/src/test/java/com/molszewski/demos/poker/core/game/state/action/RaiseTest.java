package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RaiseTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void simpleRaise() throws ActionException {
        int raiseMoney = 100;

        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());
        stateManager.executeAction(new Raise("1", raiseMoney), board, configuration);
        assertTrue(player.isReady());
        assertEquals(raiseMoney, player.getBet());
        assertEquals(configuration.startMoney() - raiseMoney, player.getMoney());
    }

    @Test
    void raiseTooLittle() throws ActionException {
        int raiseMoney = configuration.ante() + 1;

        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Raise("1", raiseMoney), board, configuration));
    }

    @Test
    void raiseTooMuch() throws ActionException {
        int raiseMoney = configuration.startMoney() + 1;

        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Raise("1", raiseMoney), board, configuration));
    }

    @Test
    void raiseChangesReadyStatus() throws ActionException {
        int raiseMoney = 2 * configuration.ante();

        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());
        assertFalse(player.isReady());
        stateManager.executeAction(new Check("1"), board, configuration);
        assertTrue(player.isReady());
        stateManager.executeAction(new Raise("2", raiseMoney), board, configuration);
        assertFalse(player.isReady());
    }
}
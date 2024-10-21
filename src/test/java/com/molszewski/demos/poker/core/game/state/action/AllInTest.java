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

class AllInTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void simpleAllIn() throws ActionException {
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
        stateManager.executeAction(new AllIn("1"), board, configuration);
        assertTrue(player.isReady());
        assertEquals(configuration.startMoney(), player.getBet());
        assertEquals(0, player.getMoney());
    }

    @Test
    void allInNotChangingReady() throws ActionException {
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
        stateManager.executeAction(new Raise("1", configuration.startMoney()), board, configuration);
        assertTrue(player.isReady());
        stateManager.executeAction(new AllIn("2"), board, configuration);
        assertTrue(player.isReady());
    }

    @Test
    void allInChangingReady() throws ActionException {
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
        stateManager.executeAction(new Check("1"), board, configuration);
        assertTrue(player.isReady());
        stateManager.executeAction(new AllIn("2"), board, configuration);
        assertFalse(player.isReady());
    }
}
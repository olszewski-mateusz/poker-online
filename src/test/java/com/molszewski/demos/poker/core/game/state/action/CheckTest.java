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

class CheckTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void simpleCheck() throws ActionException {
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
        assertEquals(configuration.firstBet(), player.getBid());
        assertEquals(configuration.startMoney() - configuration.firstBet(), player.getMoney());
    }

    @Test
    void raiseAndCheck() throws ActionException {
        int raiseMoney = 100;

        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("3"), board, configuration);
        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);

        stateManager.executeAction(new Raise("1", raiseMoney), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("2", player.getId());
        stateManager.executeAction(new Check("2"), board, configuration);
        assertTrue(player.isReady());
        assertEquals(raiseMoney, player.getBid());
        assertEquals(configuration.startMoney() - raiseMoney, player.getMoney());
    }

    @Test
    void playerNotFound() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("3"), board, configuration);
        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);

        assertThrows(ActionException.class, () -> stateManager.executeAction(new Check("4"), board, configuration));
    }

    @Test
    void checkNoMoney() throws ActionException {
        int raiseMoney = 2000;

        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("3"), board, configuration);
        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);

        stateManager.getCurrentPlayer().addMoney(raiseMoney);
        stateManager.executeAction(new Raise("1", raiseMoney), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("2", player.getId());
        assertEquals(raiseMoney, board.getCurrentBid());
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Check("2"), board, configuration));
    }
}
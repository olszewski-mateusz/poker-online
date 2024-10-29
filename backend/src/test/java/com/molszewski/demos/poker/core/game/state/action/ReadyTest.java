package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.Board;
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
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("2", true), board, configuration);

        assertEquals(1, board.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    void morePlayerReady() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);
        stateManager.executeAction(new Join("4"), board, configuration);

        stateManager.executeAction(new Ready("4", true), board, configuration);
        stateManager.executeAction(new Ready("1", true), board, configuration);
        stateManager.executeAction(new Ready("3", true), board, configuration);

        assertEquals(3, board.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    void playerReadyTwoTimes() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("2", true), board, configuration);
        assertThrows(ActionException.class,
                () -> stateManager.executeAction(new Ready("2", true), board, configuration));
    }

    @Test
    void playerNotReady() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        assertThrows(ActionException.class,
                () -> stateManager.executeAction(new Ready("2", false), board, configuration));
    }

    @Test
    void playerReadyThenNotReady() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("2", true), board, configuration);
        stateManager.executeAction(new Ready("2", false), board, configuration);

        assertFalse(board.getPlayerById("2").get().isReady());
    }
}
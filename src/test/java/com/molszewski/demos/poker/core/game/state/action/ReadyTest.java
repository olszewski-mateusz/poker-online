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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        stateManager.executeAction(new Ready("2"), board, configuration);

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

        stateManager.executeAction(new Ready("4"), board, configuration);
        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        assertEquals(3, board.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    void playerReadyTwoTimes() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("2"), board, configuration);
        assertThrows(ActionException.class,
                () -> stateManager.executeAction(new Ready("2"), board, configuration));
    }
}
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

class JoinTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void onePlayerCorrectlyAdded() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);

        assertEquals(1, board.getPlayers().size());
        Player player = board.getPlayers().getFirst();
        assertEquals(configuration.startMoney(), player.getMoney());
        assertNull(player.getHand());
        assertEquals(0, player.getBid());
        assertEquals("1", player.getId());
        assertFalse(player.isReady());
        assertFalse(player.isFolded());
    }

    @Test
    void threePlayerCorrectlyAdded() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        assertEquals(3, board.getPlayers().size());
    }

    @Test
    void samePlayerAddedTwoTimes() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        assertThrows(ActionException.class,
                () -> stateManager.executeAction(new Join("1"), board, configuration));
    }
}
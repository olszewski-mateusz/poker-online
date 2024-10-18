package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.hand.Hand;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReplaceTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void simpleReplace() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        stateManager.executeAction(new Check("1"), board, configuration);
        stateManager.executeAction(new Check("2"), board, configuration);
        stateManager.executeAction(new Check("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());

        List<Card> cards = List.copyOf(player.getHand().getCards());
        stateManager.executeAction(new Replace("1", Set.of(cards.get(0), cards.get(1))), board, configuration);
        assertTrue(player.isReady());
        Hand hand = player.getHand();
        assertTrue(hand.getCards().containsAll(Set.of(cards.get(2), cards.get(3), cards.get(4))));
        assertFalse(hand.getCards().contains(cards.get(0)));
        assertFalse(hand.getCards().contains(cards.get(1)));
        assertEquals(5, hand.getCards().size());
    }

    @Test
    void replaceIllegalCard() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        stateManager.executeAction(new Check("1"), board, configuration);
        stateManager.executeAction(new Check("2"), board, configuration);
        stateManager.executeAction(new Check("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());

        Card wrongCard = board.getDeck().pop();

        List<Card> cards = List.copyOf(player.getHand().getCards());
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Replace("1", Set.of(wrongCard, cards.get(1))), board, configuration));
    }

    @Test
    void replaceAll() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        stateManager.executeAction(new Check("1"), board, configuration);
        stateManager.executeAction(new Check("2"), board, configuration);
        stateManager.executeAction(new Check("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());

        List<Card> cards = List.copyOf(player.getHand().getCards());
        stateManager.executeAction(new Replace("1", Set.of(cards.get(0), cards.get(1), cards.get(2), cards.get(3), cards.get(4))), board, configuration);
        assertTrue(player.isReady());
        Hand hand = player.getHand();
        assertFalse(hand.getCards().contains(cards.get(0)));
        assertFalse(hand.getCards().contains(cards.get(1)));
        assertFalse(hand.getCards().contains(cards.get(2)));
        assertFalse(hand.getCards().contains(cards.get(3)));
        assertFalse(hand.getCards().contains(cards.get(4)));
        assertEquals(5, hand.getCards().size());
    }

    @Test
    void replaceNone() throws ActionException {
        Board board = new Board(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), board, configuration);
        stateManager.executeAction(new Join("2"), board, configuration);
        stateManager.executeAction(new Join("3"), board, configuration);

        stateManager.executeAction(new Ready("1"), board, configuration);
        stateManager.executeAction(new Ready("2"), board, configuration);
        stateManager.executeAction(new Ready("3"), board, configuration);

        stateManager.executeAction(new Check("1"), board, configuration);
        stateManager.executeAction(new Check("2"), board, configuration);
        stateManager.executeAction(new Check("3"), board, configuration);

        Player player = stateManager.getCurrentPlayer();
        assertEquals("1", player.getId());

        List<Card> cards = List.copyOf(player.getHand().getCards());
        stateManager.executeAction(new Replace("1", Set.of()), board, configuration);
        assertTrue(player.isReady());
        Hand hand = player.getHand();
        assertTrue(hand.getCards().containsAll(Set.of(cards.get(0), cards.get(1), cards.get(2), cards.get(3), cards.get(4))));
        assertEquals(5, hand.getCards().size());
    }
}
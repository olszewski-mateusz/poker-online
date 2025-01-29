package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.hand.Hand;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReplaceTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly replaces 2 cards")
    void simpleReplace() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);
        stateManager.executeAction(new Ready("3",true), gameState, configuration);

        stateManager.executeAction(new Check("1"), gameState, configuration);
        stateManager.executeAction(new Check("2"), gameState, configuration);
        stateManager.executeAction(new Check("3"), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());

        int cardsInDeckBeforeReplace = gameState.getDeck().getCards().size();

        List<Card> cards = List.copyOf(player.getHand().getCards());
        stateManager.executeAction(new Replace("1", Set.of(cards.get(0), cards.get(1))), gameState, configuration);
        assertTrue(player.isReady());
        Hand hand = player.getHand();
        assertTrue(hand.getCards().containsAll(Set.of(cards.get(2), cards.get(3), cards.get(4))));
        assertFalse(hand.getCards().contains(cards.get(0)));
        assertFalse(hand.getCards().contains(cards.get(1)));
        assertEquals(5, hand.getCards().size());
        assertTrue(gameState.getDeck().getDiscards().containsAll(Set.of(cards.get(0), cards.get(1))));
        assertEquals(cardsInDeckBeforeReplace - 2, gameState.getDeck().getCards().size());
    }

    @Test
    @DisplayName("Player replaces card which he doesn't have - throws error")
    void replaceIllegalCard() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);
        stateManager.executeAction(new Ready("3",true), gameState, configuration);

        stateManager.executeAction(new Check("1"), gameState, configuration);
        stateManager.executeAction(new Check("2"), gameState, configuration);
        stateManager.executeAction(new Check("3"), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());

        Card wrongCard = gameState.getDeck().pop();

        List<Card> cards = List.copyOf(player.getHand().getCards());
        assertThrows(ActionException.class, () -> stateManager.executeAction(new Replace("1", Set.of(wrongCard, cards.get(1))), gameState, configuration));
    }

    @Test
    @DisplayName("Player correctly replaces all cards")
    void replaceAll() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);
        stateManager.executeAction(new Ready("3",true), gameState, configuration);

        stateManager.executeAction(new Check("1"), gameState, configuration);
        stateManager.executeAction(new Check("2"), gameState, configuration);
        stateManager.executeAction(new Check("3"), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());

        int cardsInDeckBeforeReplace = gameState.getDeck().getCards().size();

        List<Card> cards = List.copyOf(player.getHand().getCards());
        stateManager.executeAction(new Replace("1", Set.of(cards.get(0), cards.get(1), cards.get(2), cards.get(3), cards.get(4))), gameState, configuration);
        assertTrue(player.isReady());
        Hand hand = player.getHand();
        assertFalse(hand.getCards().contains(cards.get(0)));
        assertFalse(hand.getCards().contains(cards.get(1)));
        assertFalse(hand.getCards().contains(cards.get(2)));
        assertFalse(hand.getCards().contains(cards.get(3)));
        assertFalse(hand.getCards().contains(cards.get(4)));
        assertEquals(5, hand.getCards().size());

        assertTrue(gameState.getDeck().getDiscards().containsAll(Set.of(cards.get(0), cards.get(1), cards.get(2), cards.get(3), cards.get(4))));
        assertEquals(cardsInDeckBeforeReplace - 5, gameState.getDeck().getCards().size());
    }

    @Test
    @DisplayName("Player correctly replaces zero cards")
    void replaceNone() throws ActionException {
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();

        stateManager.executeAction(new Join("1"), gameState, configuration);
        stateManager.executeAction(new Join("2"), gameState, configuration);
        stateManager.executeAction(new Join("3"), gameState, configuration);

        stateManager.executeAction(new Ready("1",true), gameState, configuration);
        stateManager.executeAction(new Ready("2",true), gameState, configuration);
        stateManager.executeAction(new Ready("3",true), gameState, configuration);

        stateManager.executeAction(new Check("1"), gameState, configuration);
        stateManager.executeAction(new Check("2"), gameState, configuration);
        stateManager.executeAction(new Check("3"), gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());

        int cardsInDeckBeforeReplace = gameState.getDeck().getCards().size();

        List<Card> cards = List.copyOf(player.getHand().getCards());
        stateManager.executeAction(new Replace("1", Set.of()), gameState, configuration);
        assertTrue(player.isReady());
        Hand hand = player.getHand();
        assertTrue(hand.getCards().containsAll(Set.of(cards.get(0), cards.get(1), cards.get(2), cards.get(3), cards.get(4))));
        assertEquals(5, hand.getCards().size());
        assertEquals(0, gameState.getDeck().getDiscards().size());
        assertEquals(cardsInDeckBeforeReplace, gameState.getDeck().getCards().size());
    }
}
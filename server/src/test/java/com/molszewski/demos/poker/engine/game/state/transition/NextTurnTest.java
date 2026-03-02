package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class NextTurnTest {

    @Test
    void nextTurnSkipsFoldedAndZeroChipPlayersInBettingPhases() {
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(
                new Player("1", "1", 100, false, true, null),
                new Player("2", "2", 0, false, false, null),
                new Player("3", "3", 100, false, false, null),
                new Player("4", "4", 100, false, false, null)
        ));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("2"));

        ActionSteps.nextTurn(gameState);

        assertEquals("3", gameState.getCurrentPlayer().getId());
    }

    @Test
    void allReadyInFirstBettingTransitionsToDrawing() {
        Player p1 = new Player("1", "1", 100, true, false, null);
        Player p2 = new Player("2", "2", 100, true, false, null);
        Player p3 = new Player("3", "3", 100, true, false, null);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(p1, p2, p3));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(p1);
        gameState.getPotState().addAnte(p1, 5);
        gameState.getPotState().addAnte(p2, 5);
        gameState.getPotState().addAnte(p3, 5);

        ActionSteps.nextTurn(gameState);

        assertEquals(GamePhase.DRAWING, gameState.getGamePhase());
        assertFalse(gameState.getPlayerById("1").isReady());
        assertFalse(gameState.getPlayerById("2").isReady());
        assertFalse(gameState.getPlayerById("3").isReady());
    }

    @Test
    void allReadyInSecondBettingTransitionsToShowdownAndPaysWinner() {
        Hand winningHand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.TEN, Suit.CLUBS)
        ));
        Hand losingHand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS)
        ));

        Player p1 = new Player("1", "1", 100, true, false, winningHand);
        Player p2 = new Player("2", "2", 100, true, false, losingHand);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(p1, p2));
        gameState.setGamePhase(GamePhase.SECOND_BETTING);
        gameState.setCurrentPlayer(p1);
        gameState.getPotState().addAnte(p1, 50);
        gameState.getPotState().addAnte(p2, 50);

        ActionSteps.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertNull(gameState.getCurrentPlayer());
        assertEquals(150, p1.getChips());
        assertEquals(50, p2.getChips());

        var winnerEntry = gameState.getHistory().getEntries().stream()
                .filter(entry -> "POT_WINNER".equals(entry.getEntryCategory()))
                .findFirst()
                .orElseThrow();

        assertEquals("100", winnerEntry.getProperties().get("potChips"));
        assertEquals("1", winnerEntry.getProperties().get("winners"));
        assertEquals("true", winnerEntry.getProperties().get("showdown"));
    }

    @Test
    void drawingWithOnlyOnePlayerAbleToBetTransitionsToShowdown() {
        Hand winningHand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.TEN, Suit.CLUBS)
        ));
        Hand losingHand1 = new Hand(Set.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS)
        ));
        Hand losingHand2 = new Hand(Set.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.EIGHT, Suit.SPADES)
        ));

        Player p1 = new Player("1", "1", 10, true, false, winningHand);
        Player p2 = new Player("2", "2", 5, true, false, losingHand1);
        Player p3 = new Player("3", "3", 5, true, false, losingHand2);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(p1, p2, p3));
        gameState.setGamePhase(GamePhase.DRAWING);
        gameState.setCurrentPlayer(p1);
        gameState.getPotState().addAnte(p1, 5);
        gameState.getPotState().addAnte(p2, 5);
        gameState.getPotState().addAnte(p3, 5);

        ActionSteps.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertNull(gameState.getCurrentPlayer());
        assertEquals(20, p1.getChips());
        assertEquals(0, p2.getChips());
        assertEquals(0, p3.getChips());
    }
}

package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.hand.Hand;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StartRoundTest {

    private final GameConfiguration gameConfiguration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Apply without start round")
    void applyNoStartTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 0, true, false, null),
                new Player("2", 100, 0, false, false, null),
                new Player("3", 100, 0, true, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        gameState.setGamePhase(GamePhase.NOT_STARTED);

        StartRound transition = StartRound.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.NOT_STARTED, gameState.getGamePhase());
    }

    @Test
    @DisplayName("Apply with round start")
    void applyStartTest() {
        Deck deck = new Deck(new Random(17));
        Player player1 = new Player("1", gameConfiguration.startMoney(), 0, true, false, null);
        Player player2 = new Player("2", gameConfiguration.startMoney(), 0, true, false, null);
        Player player3 = new Player("3", 0, 0, true, false, null);

        GameState gameState = new GameState(deck, List.of(player1, player2, player3));
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        gameState.setGamePhase(GamePhase.NOT_STARTED);

        StartRound transition = StartRound.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());

        assertTrue(!player1.isReady() && !player1.isFolded());
        assertTrue(!player2.isReady() && !player2.isFolded());
        assertTrue(!player3.isReady() && player3.isFolded());

        assertSame(player1, gameState.getCurrentPlayer());

        assertEquals(5, player1.getHand().getCards().size());
        assertEquals(5, player2.getHand().getCards().size());
        assertNull(player3.getHand());

        int expectedMoney = gameConfiguration.startMoney() - gameConfiguration.ante();

        assertEquals(expectedMoney, player1.getMoney());
        assertEquals(expectedMoney, player2.getMoney());
        assertEquals(0, player3.getMoney());
    }

    @Test
    @DisplayName("Apply with round restart")
    void applyRestartTest() {
        Hand winHand = new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.CLUBS),
                new Card(Rank.SIX, Suit.CLUBS)
        ));
        Hand loseHand = new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.SIX, Suit.CLUBS)
        ));
        int betAmount = 50;
        Deck deck = new Deck(new Random(17));
        Player player1 = new Player("1", gameConfiguration.startMoney(), betAmount, true, false, loseHand);
        Player player2 = new Player("2", gameConfiguration.startMoney(), betAmount, true, false, winHand);
        Player player3 = new Player("3", 0, betAmount, true, false, loseHand);

        GameState gameState = new GameState(deck, List.of(player1, player2, player3));
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        StartRound transition = StartRound.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());

        assertTrue(!player1.isReady() && !player1.isFolded());
        assertTrue(!player2.isReady() && !player2.isFolded());
        assertTrue(!player3.isReady() && player3.isFolded());

        assertSame(player2, gameState.getCurrentPlayer());
        assertSame(player1, gameState.getPlayers().getLast());

        assertEquals(5, player1.getHand().getCards().size());
        assertEquals(5, player2.getHand().getCards().size());
        assertNull(player3.getHand());

        int expectedMoney = gameConfiguration.startMoney() - gameConfiguration.ante();

        assertEquals(expectedMoney, player1.getMoney());
        assertEquals(expectedMoney + 3 * betAmount, player2.getMoney());
        assertEquals(0, player3.getMoney());

        assertEquals(15, gameState.getDeck().getDiscards().size());
    }

    @Test
    @DisplayName("Apply with round restart without winner")
    void applyRestartNoWinnerThrowsTest() {
        Player player1 = new Player("1", gameConfiguration.startMoney(), 0, true, true, null);
        Player player2 = new Player("2", gameConfiguration.startMoney(), 0, true, true, null);

        GameState gameState = new GameState(null, List.of(player1, player2));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        StartRound transition = StartRound.of();

        assertThrows(IllegalStateException.class, () -> transition.apply(gameState, gameConfiguration));
    }

    @Test
    @DisplayName("Apply with game finished")
    void applyGameFinishedTest() {
        Hand winHand = new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.CLUBS),
                new Card(Rank.SIX, Suit.CLUBS)
        ));
        Hand loseHand = new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.SIX, Suit.CLUBS)
        ));

        int betAmount = 40;
        Deck deck = new Deck(new Random(17));
        Player player1 = new Player("1", 0, betAmount, true, false, winHand);
        Player player2 = new Player("2", 0, betAmount, true, false, loseHand);

        GameState gameState = new GameState(deck, List.of(player1, player2));
        gameState.setCurrentPlayer(gameState.getPlayerById("1").get());
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        StartRound transition = StartRound.of();

        transition.apply(gameState, gameConfiguration);

        assertEquals(GamePhase.FINISHED, gameState.getGamePhase());
        assertEquals(2 * betAmount, player1.getMoney());
        assertEquals(0, player2.getMoney());
    }

}
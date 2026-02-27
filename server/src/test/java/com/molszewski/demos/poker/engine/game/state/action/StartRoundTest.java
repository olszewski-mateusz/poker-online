package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.history.PotWinnerEntry;
import com.molszewski.demos.poker.engine.game.state.history.ShowdownSummaryEntry;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class StartRoundTest {

    private final GameConfiguration gameConfiguration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Apply without start round")
    void applyNoStartTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 0, true, false, null),
                new Player("2", "2", 100, 0, false, false, null),
                new Player("3", "3", 100, 0, true, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));
        gameState.setGamePhase(GamePhase.NOT_STARTED);

        ActionFlow.tryStartRound(gameState, gameConfiguration);

        assertEquals(GamePhase.NOT_STARTED, gameState.getGamePhase());
    }

    @Test
    @DisplayName("Apply with round start")
    void applyStartTest() {
        Deck deck = new Deck(new Random(17));
        Player player1 = new Player("1", "1", gameConfiguration.startChips(), 0, true, false, null);
        Player player2 = new Player("2", "2", gameConfiguration.startChips(), 0, true, false, null);
        Player player3 = new Player("3", "3", 0, 0, true, false, null);

        GameState gameState = new GameState(deck, List.of(player1, player2, player3));
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));
        gameState.setGamePhase(GamePhase.NOT_STARTED);

        ActionFlow.tryStartRound(gameState, gameConfiguration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());

        assertTrue(!player1.isReady() && !player1.isFolded());
        assertTrue(!player2.isReady() && !player2.isFolded());
        assertTrue(!player3.isReady() && player3.isFolded());

        assertSame(player1, gameState.getCurrentPlayer());

        assertEquals(5, player1.getHand().getCards().size());
        assertEquals(5, player2.getHand().getCards().size());
        assertNull(player3.getHand());

        int expectedMoney = gameConfiguration.startChips() - gameConfiguration.ante();

        assertEquals(expectedMoney, player1.getChips());
        assertEquals(expectedMoney, player2.getChips());
        assertEquals(0, player3.getChips());
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
        Player player1 = new Player("1", "1", gameConfiguration.startChips(), betAmount, true, false, loseHand);
        Player player2 = new Player("2", "2", gameConfiguration.startChips(), betAmount, true, false, winHand);
        Player player3 = new Player("3", "3", 0, betAmount, true, false, loseHand);

        GameState gameState = new GameState(deck, List.of(player1, player2, player3));
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        ActionFlow.tryStartRound(gameState, gameConfiguration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());

        assertTrue(!player1.isReady() && !player1.isFolded());
        assertTrue(!player2.isReady() && !player2.isFolded());
        assertTrue(!player3.isReady() && player3.isFolded());

        assertSame(player2, gameState.getCurrentPlayer());
        assertSame(player1, gameState.getPlayers().getLast());

        assertEquals(5, player1.getHand().getCards().size());
        assertEquals(5, player2.getHand().getCards().size());
        assertNull(player3.getHand());

        int expectedMoney = gameConfiguration.startChips() - gameConfiguration.ante();

        assertEquals(expectedMoney, player1.getChips());
        assertEquals(expectedMoney + 3 * betAmount, player2.getChips());
        assertEquals(0, player3.getChips());

        assertEquals(15, gameState.getDeck().getDiscards().size());
    }

    @Test
    @DisplayName("Apply with round restart without winner")
    void applyRestartNoWinnerThrowsTest() {
        Player player1 = new Player("1", "1", gameConfiguration.startChips(), 0, true, true, null);
        Player player2 = new Player("2", "2", gameConfiguration.startChips(), 0, true, true, null);

        GameState gameState = new GameState(null, List.of(player1, player2));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        assertThrows(IllegalStateException.class, () -> ActionFlow.tryStartRound(gameState, gameConfiguration));
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
        Player player1 = new Player("1", "1", 0, betAmount, true, false, winHand);
        Player player2 = new Player("2", "2", 0, betAmount, true, false, loseHand);

        GameState gameState = new GameState(deck, List.of(player1, player2));
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        ActionFlow.tryStartRound(gameState, gameConfiguration);

        assertEquals(GamePhase.FINISHED, gameState.getGamePhase());
        assertEquals(2 * betAmount, player1.getChips());
        assertEquals(0, player2.getChips());
    }

    @Test
    @DisplayName("Apply restart distributes side pots correctly")
    void applyRestartWithSidePotsTest() {
        GameConfiguration zeroAnteConfiguration = new GameConfiguration(2, 1000, 0);
        Player player1 = new Player("1", "1", 0, 100, true, false, new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.TEN, Suit.SPADES)
        )));
        Player player2 = new Player("2", "2", 0, 50, true, false, new Hand(Set.of(
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.DIAMONDS),
                new Card(Rank.NINE, Suit.CLUBS)
        )));
        Player player3 = new Player("3", "3", 0, 20, true, false, new Hand(Set.of(
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.NINE, Suit.DIAMONDS),
                new Card(Rank.EIGHT, Suit.CLUBS)
        )));

        GameState gameState = new GameState(null, List.of(player1, player2, player3));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        ActionFlow.tryStartRound(gameState, zeroAnteConfiguration);

        assertEquals(GamePhase.FINISHED, gameState.getGamePhase());
        assertEquals(170, player1.getChips());
        assertEquals(0, player2.getChips());
        assertEquals(0, player3.getChips());
        assertEquals(0, player1.getBet());
        assertEquals(0, player2.getBet());
        assertEquals(0, player3.getBet());

        List<PotWinnerEntry> potWinnerEntries = gameState.getHistory().getEntries().stream()
                .filter(PotWinnerEntry.class::isInstance)
                .map(PotWinnerEntry.class::cast)
                .toList();
        assertEquals(3, potWinnerEntries.size());
        assertEquals(List.of(60, 60, 50), potWinnerEntries.stream().map(PotWinnerEntry::getPotAmount).toList());
        assertEquals(List.of("1"), potWinnerEntries.get(0).getWinnerIds());
        assertEquals(List.of("1"), potWinnerEntries.get(1).getWinnerIds());
        assertEquals(List.of("1"), potWinnerEntries.get(2).getWinnerIds());
        assertEquals(
                170,
                gameState.getPlayers().stream().mapToInt(Player::getChips).sum(),
                "Total chips after distribution must equal total committed bets"
        );

        ShowdownSummaryEntry showdownSummaryEntry = gameState.getHistory().getEntries().stream()
                .filter(ShowdownSummaryEntry.class::isInstance)
                .map(ShowdownSummaryEntry.class::cast)
                .findFirst()
                .orElseThrow();
        assertEquals(3, showdownSummaryEntry.getPots().size());
        assertEquals(List.of(60, 60, 50), showdownSummaryEntry.getPots().stream()
                .map(ShowdownSummaryEntry.PotSummary::potAmount)
                .toList());
        assertEquals(List.of("1"), showdownSummaryEntry.getWinners().stream()
                .map(ShowdownSummaryEntry.PlayerSummary::playerId)
                .toList());
        assertNotNull(showdownSummaryEntry.getWinners().getFirst().handType());
        assertFalse(showdownSummaryEntry.getWinners().getFirst().keyCards().isEmpty());
    }

    @Test
    @DisplayName("Apply restart splits tied pot and assigns odd chip by order")
    void applyRestartWithTiedPotTest() {
        GameConfiguration zeroAnteConfiguration = new GameConfiguration(2, 1000, 0);
        Player player1 = new Player("1", "1", 0, 101, true, false, new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.NINE, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS)
        )));
        Player player2 = new Player("2", "2", 0, 101, true, false, new Hand(Set.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.NINE, Suit.SPADES),
                new Card(Rank.SEVEN, Suit.SPADES),
                new Card(Rank.THREE, Suit.SPADES)
        )));
        Player player3 = new Player("3", "3", 0, 101, true, true, null);

        GameState gameState = new GameState(new Deck(new Random(17)), List.of(player1, player2, player3));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        ActionFlow.tryStartRound(gameState, zeroAnteConfiguration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());
        assertEquals(152, player1.getChips());
        assertEquals(151, player2.getChips());
        assertEquals(0, player3.getChips());
    }

    @Test
    @DisplayName("Apply start round handles short ante as all in")
    void applyStartWithShortAnteTest() {
        Player shortStackPlayer = new Player("1", "1", 3, 0, true, false, null);
        Player richPlayer = new Player("2", "2", 100, 0, true, false, null);
        GameState gameState = new GameState(new Deck(new Random(17)), List.of(shortStackPlayer, richPlayer));
        gameState.setGamePhase(GamePhase.NOT_STARTED);

        ActionFlow.tryStartRound(gameState, gameConfiguration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());
        assertEquals(0, shortStackPlayer.getChips());
        assertEquals(3, shortStackPlayer.getBet());
        assertTrue(shortStackPlayer.isReady());
        assertFalse(shortStackPlayer.isFolded());

        assertEquals(95, richPlayer.getChips());
        assertEquals(5, richPlayer.getBet());
        assertFalse(richPlayer.isReady());
        assertEquals(richPlayer, gameState.getCurrentPlayer());
    }

}


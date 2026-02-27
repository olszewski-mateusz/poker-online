package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class GameStateTest {

    @Test
    @DisplayName("Get player by id")
    void getPlayerByIdTest() {
        List<Player> players = List.of(
                new Player("A", "A", 100),
                new Player("B", "B", 100),
                new Player("C", "C", 100)
        );
        GameState gameState = new GameState(null, players);

        Player player = gameState.getPlayerById("B");
        assertSame(players.get(1), player);
    }

    @Test
    @DisplayName("Get not existing player by id")
    void getPlayerByIdEmptyTest() {
        List<Player> players = List.of(
                new Player("A", "A", 100),
                new Player("B", "B", 100),
                new Player("C", "C", 100)
        );
        GameState gameState = new GameState(null, players);

        assertThrows(NoSuchElementException.class, () -> gameState.getPlayerById("D"));
    }

    @Test
    @DisplayName("Get current bet")
    void getCurrentBetTest() {
        List<Player> players = List.of(
                new Player("A", "A", 100, 10, false, false, null),
                new Player("B", "B", 100, 100, false, false, null),
                new Player("C", "C", 100, 10, false, false, null)
        );
        GameState gameState = new GameState(null, players);

        assertEquals(100, gameState.getCurrentBet());
    }

    @Test
    @DisplayName("Get current bet when no players - throws")
    void getCurrentBetNoPlayersTest() {
        GameState gameState = new GameState(null, List.of());

        assertThrows(IllegalStateException.class, gameState::getCurrentBet);
    }

    @Test
    @DisplayName("No open bet when active players have equal committed bets")
    void isBetPlacedInCurrentPhaseFalseForEqualActiveBets() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 5, false, false, null),
                new Player("2", "2", 100, 5, false, false, null),
                new Player("3", "3", 0, 2, true, false, null)
        ));

        assertFalse(gameState.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Open bet when active players have different committed bets")
    void isBetPlacedInCurrentPhaseTrueForDifferentActiveBets() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 5, false, false, null),
                new Player("2", "2", 100, 10, false, false, null),
                new Player("3", "3", 0, 10, true, false, null)
        ));

        assertTrue(gameState.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Open bet when highest committed bet belongs to all in player")
    void isBetPlacedInCurrentPhaseTrueWhenHighestBetIsAllInPlayer() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 0, 30, true, false, null),
                new Player("2", "2", 100, 20, false, false, null),
                new Player("3", "3", 100, 30, false, false, null)
        ));

        assertTrue(gameState.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Get winners when showdown phase")
    void getWinnersShowdownTest() {
        Hand winnerHand = new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.CLUBS)
        ));

        Hand loserHand = new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.CLUBS)
        ));

        Player winnerPlayer = new Player("1", "1", 100, 100, false, false, winnerHand);
        Player foldedPlayer = new Player("2", "2", 100, 100, false, true, winnerHand);
        Player loserPlayer = new Player("3", "3", 100, 100, false, false, loserHand);

        GameState gameState = new GameState(null, List.of(winnerPlayer, foldedPlayer, loserPlayer));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        List<Player> winners = gameState.getWinners();
        assertEquals(1, winners.size());
        assertSame(winnerPlayer, winners.getFirst());
    }

    @Test
    @DisplayName("Get winners when showdown has tie")
    void getWinnersShowdownTieTest() {
        Hand sharedStrengthA = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.NINE, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS)
        ));
        Hand sharedStrengthB = new Hand(Set.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.NINE, Suit.SPADES),
                new Card(Rank.SEVEN, Suit.SPADES),
                new Card(Rank.THREE, Suit.SPADES)
        ));
        Hand weakerHand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.HEARTS),
                new Card(Rank.TWO, Suit.HEARTS)
        ));

        Player winnerA = new Player("1", "1", 100, 100, false, false, sharedStrengthA);
        Player winnerB = new Player("2", "2", 100, 100, false, false, sharedStrengthB);
        Player loser = new Player("3", "3", 100, 100, false, false, weakerHand);

        GameState gameState = new GameState(null, List.of(winnerA, winnerB, loser));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        List<Player> winners = gameState.getWinners();
        assertEquals(2, winners.size());
        assertEquals(List.of(winnerA, winnerB), winners);
    }

    @Test
    @DisplayName("Showdown throws when active player has no hand")
    void getWinnersShowdownMissingHandThrows() {
        Hand hand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.NINE, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS)
        ));
        Player playerWithHand = new Player("1", "1", 100, 100, false, false, hand);
        Player playerWithoutHand = new Player("2", "2", 100, 100, false, false, null);
        GameState gameState = new GameState(null, List.of(playerWithHand, playerWithoutHand));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        IllegalStateException exception = assertThrows(IllegalStateException.class, gameState::getWinners);
        assertEquals("Showdown invariant violated: non-folded player without hand", exception.getMessage());
    }

    @Test
    @DisplayName("Get winners when finished phase")
    void getWinnersFinishedTest() {
        Player winner = new Player("winner", "winner", 100, 100, false, false, null);
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, false, true, null),
                new Player("2", "2", 100, 100, false, true, null),
                new Player("3", "3", 100, 100, false, true, null),
                winner,
                new Player("4", "4", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FINISHED);

        List<Player> winners = gameState.getWinners();
        assertEquals(1, winners.size());
        assertSame(winner, winners.getFirst());
    }

    @Test
    @DisplayName("Get winners when in game phase")
    void getWinnersEmptyTest() {
        GameState gameState = new GameState(null, List.of());
        gameState.setGamePhase(GamePhase.DRAWING);
        List<Player> winners = gameState.getWinners();
        assertTrue(winners.isEmpty());
    }

    @Test
    @DisplayName("Finished throws when more than one player is active")
    void getWinnersFinishedMultipleActiveThrows() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, false, false, null),
                new Player("2", "2", 100, 100, false, false, null),
                new Player("3", "3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FINISHED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, gameState::getWinners);
        assertEquals("Finished invariant violated: expected exactly one non-folded player", exception.getMessage());
    }

    @Test
    @DisplayName("Add player correctly")
    void addPlayerTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100)
        ));

        gameState.addPlayer("2", "2", 100);

        assertEquals(2, gameState.getPlayers().size());
        assertEquals("2", gameState.getPlayerById("2").getId());
    }

    @Test
    @DisplayName("Add existing player - throws")
    void addPlayerThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100)
        ));

        assertThrows(IllegalStateException.class, () -> gameState.addPlayer("1", "1", 100));
    }

}


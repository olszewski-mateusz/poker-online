package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.hand.Hand;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class GameStateTest {

    @Test
    @DisplayName("Get player by id")
    void getPlayerByIdTest() {
        List<Player> players = List.of(
                new Player("A", 100),
                new Player("B", 100),
                new Player("C", 100)
        );
        GameState gameState = new GameState(null, players);

        Optional<Player> player = gameState.getPlayerById("B");

        assertTrue(player.isPresent());
        assertSame(players.get(1), player.get());
    }

    @Test
    @DisplayName("Get not existing player by id")
    void getPlayerByIdEmptyTest() {
        List<Player> players = List.of(
                new Player("A", 100),
                new Player("B", 100),
                new Player("C", 100)
        );
        GameState gameState = new GameState(null, players);

        Optional<Player> player = gameState.getPlayerById("D");

        assertTrue(player.isEmpty());
    }

    @Test
    @DisplayName("Get current bet")
    void getCurrentBetTest() {
        List<Player> players = List.of(
                new Player("A", 100, 10, false, false, null),
                new Player("B", 100, 100, false, false, null),
                new Player("C", 100, 10, false, false, null)
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
    @DisplayName("Get winner when showdown phase")
    void getWinnerShowdownTest() {
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

        Player winnerPlayer = new Player("1", 100, 100, false, false, winnerHand);
        Player foldedPlayer = new Player("2", 100, 100, false, true, winnerHand);
        Player loserPlayer = new Player("3", 100, 100, false, false, loserHand);

        GameState gameState = new GameState(null, List.of(winnerPlayer, foldedPlayer, loserPlayer));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        Optional<Player> result = gameState.getWinner();
        assertTrue(result.isPresent());
        assertSame(winnerPlayer, result.get());
    }

    @Test
    @DisplayName("Get winner when finished phase")
    void getWinnerFinishedTest() {
        Player winner = new Player("winner", 100, 100, false, false, null);
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100, 100, false, true, null),
                new Player("2", 100, 100, false, true, null),
                new Player("3", 100, 100, false, true, null),
                winner,
                new Player("4", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FINISHED);

        Optional<Player> result = gameState.getWinner();
        assertTrue(result.isPresent());
        assertSame(winner, result.get());
    }

    @Test
    @DisplayName("Get winner when in game phase")
    void getWinnerEmptyTest() {
        GameState gameState = new GameState(null, List.of());
        gameState.setGamePhase(GamePhase.DRAWING);
        Optional<Player> result = gameState.getWinner();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Add player correctly")
    void addPlayerTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100)
        ));

        gameState.addPlayer("2", 100);

        assertEquals(2, gameState.getPlayers().size());
        assertTrue(gameState.getPlayerById("2").isPresent());
    }

    @Test
    @DisplayName("Add existing player - throws")
    void addPlayerThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", 100)
        ));

        assertThrows(IllegalStateException.class, () -> gameState.addPlayer("1", 100));
    }

}
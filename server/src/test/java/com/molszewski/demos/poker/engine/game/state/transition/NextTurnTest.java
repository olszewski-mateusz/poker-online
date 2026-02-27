package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.history.PotWinnerEntry;
import com.molszewski.demos.poker.engine.game.state.history.WinnerEntry;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class NextTurnTest {

    @Test
    @DisplayName("Next turn without new phase from betting phase")
    void nextTurnNoNewPhaseBettingPhaseTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, false, true, null),
                new Player("2", "2", 0, 100, false, false, null),
                new Player("3", "3", 100, 100, false, false, null),
                new Player("4", "4", 100, 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("4"));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);

        ActionFlow.nextTurn(gameState);

        assertEquals("3", gameState.getCurrentPlayer().getId());
    }

    @Test
    @DisplayName("Next turn without new phase from drawing phase")
    void nextTurnNoNewPhaseDrawingPhaseTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, false, true, null),
                new Player("2", "2", 0, 100, false, false, null),
                new Player("3", "3", 100, 100, false, false, null),
                new Player("4", "4", 100, 100, false, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("4"));
        gameState.setGamePhase(GamePhase.DRAWING);

        ActionFlow.nextTurn(gameState);

        assertEquals("2", gameState.getCurrentPlayer().getId());
    }

    @Test
    @DisplayName("Next turn without new phase no players throws")
    void nextTurnNoNewPhaseNoPlayersThrowsTest() {
        GameState gameState = new GameState(null, List.of());
        assertThrows(IllegalStateException.class, () -> ActionFlow.nextTurn(gameState));
    }

    @Test
    @DisplayName("Next turn without new phase no current player throws")
    void nextTurnNoNewPhaseNoCurrentPlayerThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100),
                new Player("2", "2", 0)
        ));

        assertThrows(IllegalStateException.class, () -> ActionFlow.nextTurn(gameState));
    }

    @Test
    @DisplayName("Next turn with new phase from first betting phase")
    void nextTurnNewPhaseFromFirstBettingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 100, 100, true, false, null),
                new Player("3", "3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.DRAWING, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
    }

    @Test
    @DisplayName("Next turn with new phase from drawing phase")
    void nextTurnNewPhaseFromDrawingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 0, 100, true, false, null),
                new Player("3", "3", 100, 100, false, true, null),
                new Player("4", "4", 100, 100, true, false, null)
        ));
        gameState.setGamePhase(GamePhase.DRAWING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SECOND_BETTING, gameState.getGamePhase());
        assertFalse(gameState.getPlayerById("1").isReady());
        assertTrue(gameState.getPlayerById("2").isReady());
        assertFalse(gameState.getPlayerById("3").isReady());
        assertFalse(gameState.getPlayerById("4").isReady());
    }

    @Test
    @DisplayName("Next turn with new phase from second betting phase")
    void nextTurnNewPhaseFromSecondBettingTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, new Hand(Set.of(
                        new Card(Rank.ACE, Suit.CLUBS),
                        new Card(Rank.ACE, Suit.DIAMONDS),
                        new Card(Rank.KING, Suit.HEARTS),
                        new Card(Rank.QUEEN, Suit.SPADES),
                        new Card(Rank.TEN, Suit.CLUBS)
                ))),
                new Player("2", "2", 0, 100, true, false, new Hand(Set.of(
                        new Card(Rank.KING, Suit.CLUBS),
                        new Card(Rank.KING, Suit.DIAMONDS),
                        new Card(Rank.QUEEN, Suit.HEARTS),
                        new Card(Rank.JACK, Suit.SPADES),
                        new Card(Rank.NINE, Suit.CLUBS)
                ))),
                new Player("3", "3", 100, 100, false, true, null),
                new Player("4", "4", 100, 100, true, false, new Hand(Set.of(
                        new Card(Rank.QUEEN, Suit.CLUBS),
                        new Card(Rank.QUEEN, Suit.DIAMONDS),
                        new Card(Rank.JACK, Suit.HEARTS),
                        new Card(Rank.NINE, Suit.SPADES),
                        new Card(Rank.EIGHT, Suit.CLUBS)
                )))
        ));
        gameState.setGamePhase(GamePhase.SECOND_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("Next turn from unexpected phase throws")
    void nextTurnNewPhaseFromUnexpectedThrowsTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, null),
                new Player("2", "2", 100, 100, true, false, null)
        ));
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));
        gameState.setGamePhase(GamePhase.FINISHED);

        assertThrows(IllegalStateException.class, () -> ActionFlow.nextTurn(gameState));
    }

    @Test
    @DisplayName("Next turn from first betting phase skip to showdown")
    void nextTurnNewPhaseFromBettingAllFoldedTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, new Hand(Set.of(
                        new Card(Rank.ACE, Suit.CLUBS),
                        new Card(Rank.ACE, Suit.DIAMONDS),
                        new Card(Rank.KING, Suit.HEARTS),
                        new Card(Rank.QUEEN, Suit.SPADES),
                        new Card(Rank.TEN, Suit.CLUBS)
                ))),
                new Player("2", "2", 0, 100, true, true, null),
                new Player("3", "3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("Next turn from drawing phase skip to showdown")
    void nextTurnNewPhaseFromDrawingAllFoldedTest() {
        GameState gameState = new GameState(null, List.of(
                new Player("1", "1", 100, 100, true, false, new Hand(Set.of(
                        new Card(Rank.ACE, Suit.CLUBS),
                        new Card(Rank.ACE, Suit.DIAMONDS),
                        new Card(Rank.KING, Suit.HEARTS),
                        new Card(Rank.QUEEN, Suit.SPADES),
                        new Card(Rank.TEN, Suit.CLUBS)
                ))),
                new Player("2", "2", 0, 100, true, false, new Hand(Set.of(
                        new Card(Rank.KING, Suit.CLUBS),
                        new Card(Rank.KING, Suit.DIAMONDS),
                        new Card(Rank.QUEEN, Suit.HEARTS),
                        new Card(Rank.JACK, Suit.SPADES),
                        new Card(Rank.NINE, Suit.CLUBS)
                ))),
                new Player("3", "3", 100, 100, false, true, null)
        ));
        gameState.setGamePhase(GamePhase.DRAWING);
        gameState.setCurrentPlayer(gameState.getPlayerById("1"));

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertTrue(gameState.getPlayers().stream().noneMatch(Player::isReady));
        assertNull(gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("Next turn to showdown settles pot immediately")
    void nextTurnToShowdownSettlesPotImmediatelyTest() {
        Hand winningHand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.TEN, Suit.CLUBS)
        ));
        Hand losingHand = new Hand(Set.of(
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.NINE, Suit.CLUBS)
        ));

        Player player1 = new Player("1", "1", 0, 40, true, false, winningHand);
        Player player2 = new Player("2", "2", 0, 40, true, false, losingHand);
        GameState gameState = new GameState(null, List.of(player1, player2));
        gameState.setGamePhase(GamePhase.SECOND_BETTING);
        gameState.setCurrentPlayer(player1);

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertEquals(80, player1.getChips());
        assertEquals(0, player2.getChips());
        assertEquals(0, player1.getBet());
        assertEquals(0, player2.getBet());
        assertNull(gameState.getCurrentPlayer());
        assertEquals(1, gameState.getHistory().getEntries().stream()
                .filter(PotWinnerEntry.class::isInstance)
                .count());
    }

    @Test
    @DisplayName("Next turn showdown winner entries include side-pot winners")
    void nextTurnShowdownWinnerEntriesIncludeSidePotWinnersTest() {
        Player player1 = new Player("1", "1", 0, 20, true, false, new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.TEN, Suit.CLUBS)
        )));
        Player player2 = new Player("2", "2", 0, 50, true, false, new Hand(Set.of(
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.NINE, Suit.CLUBS)
        )));
        Player player3 = new Player("3", "3", 0, 50, true, false, new Hand(Set.of(
                new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.DIAMONDS),
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.EIGHT, Suit.SPADES),
                new Card(Rank.SEVEN, Suit.CLUBS)
        )));

        GameState gameState = new GameState(null, List.of(player1, player2, player3));
        gameState.setGamePhase(GamePhase.SECOND_BETTING);
        gameState.setCurrentPlayer(player1);

        ActionFlow.nextTurn(gameState);

        assertEquals(GamePhase.SHOWDOWN, gameState.getGamePhase());
        assertEquals(60, player1.getChips());
        assertEquals(60, player2.getChips());
        assertEquals(0, player3.getChips());

        List<String> winnerIds = gameState.getHistory().getEntries().stream()
                .filter(WinnerEntry.class::isInstance)
                .map(WinnerEntry.class::cast)
                .map(WinnerEntry::getPlayerId)
                .toList();
        assertEquals(List.of("1", "2"), winnerIds);
    }
}


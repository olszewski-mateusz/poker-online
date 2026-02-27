package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Bet;
import com.molszewski.demos.poker.engine.game.state.action.Join;
import com.molszewski.demos.poker.engine.game.state.action.Raise;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class HistoryTest {

    private History history;

    @BeforeEach
    void setUp() {
        history = new History();
    }

    @Test
    @DisplayName("Add action entry")
    void addActionTest() {
        history.addAction(new Join("1", "1"));

        List<HistoryEntry> entries = history.getEntries();
        ActionEntry entry = (ActionEntry) entries.getFirst();

        assertEquals(1, entries.size());
        assertEquals("1", entry.getAction().getPlayerId());
    }

    @Test
    @DisplayName("Raise marks bet placed in current phase")
    void addRaiseActionTest() {
        history.addAction(new Raise("1", 100));

        assertTrue(history.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Bet marks bet placed in current phase")
    void addBetActionTest() {
        history.addAction(new Bet("1", 100));

        assertTrue(history.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Phase change adds entry and resets bet flag")
    void phaseChangeTest() {
        history.addAction(new Raise("1", 100));
        history.registerFullRaiseSize(20);

        GameState state = new GameState(null, List.of());
        history.onPhaseChanged(state, GamePhase.FIRST_BETTING);

        List<HistoryEntry> entries = history.getEntries();
        PhaseChangeEntry entry = (PhaseChangeEntry) entries.get(1);

        assertEquals(2, entries.size());
        assertEquals(GamePhase.FIRST_BETTING, entry.getPhase());
        assertFalse(history.isBetPlacedInCurrentPhase());
        assertEquals(0, history.getLastFullRaiseSizeInCurrentPhase());
    }

    @Test
    @DisplayName("Register full raise size stores current phase minimum raise")
    void registerFullRaiseSizeTest() {
        history.registerFullRaiseSize(15);

        assertTrue(history.isBetPlacedInCurrentPhase());
        assertEquals(15, history.getLastFullRaiseSizeInCurrentPhase());
    }

    @Test
    @DisplayName("Add pot winner entry")
    void addPotWinnerEntryTest() {
        history.addPotWinner(1, 150, List.of("1", "2"));

        List<HistoryEntry> entries = history.getEntries();
        PotWinnerEntry entry = (PotWinnerEntry) entries.getFirst();

        assertEquals(1, entries.size());
        assertEquals(1, entry.getPotIndex());
        assertEquals(150, entry.getPotAmount());
        assertEquals(List.of("1", "2"), entry.getWinnerIds());
    }

    @Test
    @DisplayName("Add showdown summary entry")
    void addShowdownSummaryEntryTest() {
        history.addShowdownSummary(
                List.of(new ShowdownSummaryEntry.PotSummary(1, 150, List.of("1", "2"))),
                List.of(new ShowdownSummaryEntry.PlayerSummary(
                        "1",
                        com.molszewski.demos.poker.engine.hand.HandType.PAIR,
                        List.of()
                ))
        );

        List<HistoryEntry> entries = history.getEntries();
        assertEquals(1, entries.size());
        assertInstanceOf(ShowdownSummaryEntry.class, entries.getFirst());

        ShowdownSummaryEntry entry = (ShowdownSummaryEntry) entries.getFirst();
        assertEquals(1, entry.getPots().size());
        assertEquals(1, entry.getWinners().size());
        assertEquals(150, entry.getPots().getFirst().potAmount());
        assertEquals("1", entry.getWinners().getFirst().playerId());
    }

    @Test
    @DisplayName("Showdown phase change adds phase entry")
    void phaseChangeShowdownTest() {
        Player player = new Player("1", "1", 100, 0, false, false, null);
        GameState state = new GameState(null, List.of(player));
        history.onPhaseChanged(state, GamePhase.SHOWDOWN);

        List<HistoryEntry> entries = history.getEntries();
        PhaseChangeEntry phaseChangeEntry = (PhaseChangeEntry) entries.getFirst();

        assertEquals(1, entries.size());
        assertEquals(GamePhase.SHOWDOWN, phaseChangeEntry.getPhase());
    }

    @Test
    @DisplayName("Finished phase change adds game winner entry")
    void phaseChangeFinishedTest() {
        Hand hand = new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.CLUBS),
                new Card(Rank.SIX, Suit.CLUBS)
        ));
        Player player = new Player("1", "1", 100, 0, false, false, hand);
        GameState state = new GameState(null, List.of(player));
        state.setGamePhase(GamePhase.SHOWDOWN);
        history.onPhaseChanged(state, GamePhase.FINISHED);

        List<HistoryEntry> entries = history.getEntries();
        PhaseChangeEntry phaseChangeEntry = (PhaseChangeEntry) entries.getFirst();
        GameWinnerEntry winnerEntry = (GameWinnerEntry) entries.get(1);

        assertEquals(2, entries.size());
        assertEquals(GamePhase.FINISHED, phaseChangeEntry.getPhase());
        assertEquals("1", winnerEntry.getPlayerId());
    }

    @Test
    @DisplayName("Showdown phase change without winner adds only phase entry")
    void phaseChangeShowdownNoWinnerTest() {
        GameState state = new GameState(null, List.of());
        history.onPhaseChanged(state, GamePhase.SHOWDOWN);

        List<HistoryEntry> entries = history.getEntries();
        PhaseChangeEntry entry = (PhaseChangeEntry) entries.getFirst();

        assertEquals(1, entries.size());
        assertEquals(GamePhase.SHOWDOWN, entry.getPhase());
    }

    @Test
    @DisplayName("Add winner entry includes key cards")
    void addWinnerEntryWithKeyCardsTest() {
        Hand hand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.TEN, Suit.CLUBS)
        ));
        history.addWinner("1", hand.getHandType(), hand.getKeyCards());

        List<HistoryEntry> entries = history.getEntries();
        assertEquals(1, entries.size());
        assertInstanceOf(WinnerEntry.class, entries.get(0));

        WinnerEntry winnerEntry = (WinnerEntry) entries.get(0);
        assertEquals("1", winnerEntry.getPlayerId());
        assertEquals(hand.getHandType(), winnerEntry.getHandType());
        assertEquals(5, winnerEntry.getKeyCards().size());
        assertEquals(Rank.ACE, winnerEntry.getKeyCards().get(0).rank());
        assertEquals(Rank.ACE, winnerEntry.getKeyCards().get(1).rank());
    }

    @Test
    @DisplayName("Add winner entries for all winners")
    void addWinnerEntriesForAllWinnersTest() {
        Hand handA = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.NINE, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS)
        ));
        Hand handB = new Hand(Set.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.NINE, Suit.SPADES),
                new Card(Rank.SEVEN, Suit.SPADES),
                new Card(Rank.THREE, Suit.SPADES)
        ));

        history.addWinner("1", handA.getHandType(), handA.getKeyCards());
        history.addWinner("2", handB.getHandType(), handB.getKeyCards());

        List<HistoryEntry> entries = history.getEntries();
        assertEquals(2, entries.size());
        assertInstanceOf(WinnerEntry.class, entries.get(0));
        assertInstanceOf(WinnerEntry.class, entries.get(1));
        assertEquals(5, ((WinnerEntry) entries.get(0)).getKeyCards().size());
        assertEquals(5, ((WinnerEntry) entries.get(1)).getKeyCards().size());
    }
}


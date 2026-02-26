package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
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
    @DisplayName("Phase change adds entry and resets bet flag")
    void phaseChangeTest() {
        history.addAction(new Raise("1", 100));

        GameState state = new GameState(null, List.of());
        history.onPhaseChanged(state, GamePhase.FIRST_BETTING);

        List<HistoryEntry> entries = history.getEntries();
        PhaseChangeEntry entry = (PhaseChangeEntry) entries.get(1);

        assertEquals(2, entries.size());
        assertEquals(GamePhase.FIRST_BETTING, entry.getPhase());
        assertFalse(history.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Showdown phase change adds winner entry")
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
}


package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Join;
import com.molszewski.demos.poker.engine.game.state.pot.PotType;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
class HistoryTest {

    @Test
    void addActionAndFlowStepTest() {
        History history = new History();

        history.addAction(new Join("1", "Alice"));
        history.addFlowStep(FlowStep.NEXT_TURN);

        assertEquals(2, history.getEntries().size());
        assertInstanceOf(ActionEntry.class, history.getEntries().get(0));
        assertInstanceOf(FlowStepEntry.class, history.getEntries().get(1));
    }

    @Test
    void addPotWinnerAndWinnerTest() {
        History history = new History();

        history.addPotWinner(150, PotType.SIDE, Set.of("1", "2"), true);
        history.addWinner("1");

        assertEquals(2, history.getEntries().size());
        PotWinnerEntry potWinnerEntry = (PotWinnerEntry) history.getEntries().get(0);
        WinnerEntry winnerEntry = (WinnerEntry) history.getEntries().get(1);

        assertEquals(150, potWinnerEntry.getPotAmount());
        assertEquals(PotType.SIDE, potWinnerEntry.getPotType());
        assertEquals(Set.of("1", "2"), potWinnerEntry.getWinnerIds());
        assertEquals("1", winnerEntry.getPlayerId());
    }

    @Test
    void phaseChangeToFinishedAddsGameWinnerEntryTest() {
        Player winner = new Player("winner", "winner", 100, false, false, null);
        Player folded = new Player("folded", "folded", 100, false, true, null);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(winner, folded));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        History history = new History();
        history.onPhaseChanged(gameState, GamePhase.FINISHED);

        assertEquals(2, history.getEntries().size());
        assertInstanceOf(PhaseChangeEntry.class, history.getEntries().get(0));
        assertInstanceOf(GameWinnerEntry.class, history.getEntries().get(1));
        assertEquals("winner", ((GameWinnerEntry) history.getEntries().get(1)).getPlayerId());
    }
}

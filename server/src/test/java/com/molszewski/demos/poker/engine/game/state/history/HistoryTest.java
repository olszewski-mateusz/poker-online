package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.action.Ready;
import com.molszewski.demos.poker.engine.action.ActionType;
import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.history.*;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.pot.Pot;
import com.molszewski.demos.poker.engine.pot.PotType;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
class HistoryTest {

    @Test
    void addActionAndFlowStepTest() {
        GameState state = new GameState(new Deck(new Random(17L)), List.of(
                new Player("1", "Alice", 100, false, true, null)
        ));
        History history = new History();

        history.addAction(state, new Ready("1", true));
        history.addFlowStep(state, ActionStepType.NEXT_TURN);

        assertEquals(2, history.getEntries().size());
        assertEquals("ACTION", history.getEntries().get(0).getEntryCategory());
        assertEquals(ActionType.READY.toString(), history.getEntries().get(0).getProperties().get("actionType"));
        assertEquals("Alice", history.getEntries().get(0).getProperties().get("playerName"));
        assertEquals("ACTION_STEP", history.getEntries().get(1).getEntryCategory());
        assertEquals("NEXT_TURN", history.getEntries().get(1).getProperties().get("stepType"));
    }

    @Test
    void addPotWinnerAndGameWinnerTest() {
        Hand winningHand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.TEN, Suit.CLUBS)
        ));
        Player winner = new Player("1", "Alice", 100, false, false, winningHand);
        GameState state = new GameState(new Deck(new Random(17L)), List.of(winner));
        Pot pot = new Pot(PotType.SIDE, Map.of("1", 150), Set.of("1"));
        History history = new History();

        history.addPotWinner(state, pot, List.of(winner), true);
        history.addGameWinner(state, winner);

        assertEquals(2, history.getEntries().size());
        assertEquals("POT_WINNER", history.getEntries().get(0).getEntryCategory());
        assertEquals("SIDE", history.getEntries().get(0).getProperties().get("potType"));
        assertEquals("150", history.getEntries().get(0).getProperties().get("potChips"));
        assertEquals("Alice", history.getEntries().get(0).getProperties().get("winners"));
        assertEquals("true", history.getEntries().get(0).getProperties().get("showdown"));
        assertEquals("GAME_WINNER", history.getEntries().get(1).getEntryCategory());
        assertEquals("Alice", history.getEntries().get(1).getProperties().get("winnerName"));
    }

    @Test
    void phaseChangeToFinishedAddsPhaseChangeEntryTest() {
        Player winner = new Player("winner", "winner", 100, false, false, null);
        Player folded = new Player("folded", "folded", 100, false, true, null);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(winner, folded));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        History history = new History();
        history.onPhaseChanged(gameState, GamePhase.FINISHED);

        assertEquals(1, history.getEntries().size());
        assertEquals("PHASE_CHANGE", history.getEntries().get(0).getEntryCategory());
        assertEquals("FINISHED", history.getEntries().get(0).getProperties().get("newPhase"));
    }
}

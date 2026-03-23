package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.pot.Pot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class History {

    @Getter
    private final List<HistoryEntry> entries = new ArrayList<>();

    public void addAction(GameState gameState, Action action) {
        entries.add(new ActionEntry(gameState, action));
    }

    public void addFlowStep(GameState gameState, ActionStepType step) {
        entries.add(new ActionStepEntry(gameState, step));
    }

    public void addPotWinner(GameState gameState, Pot pot, List<Player> winners, boolean showdown) {
        entries.add(new PotWinnerEntry(gameState, new PotWinnerEntry.Context(pot, winners, showdown)));
    }

    public void addGameWinner(GameState gameState, Player winner) {
        entries.add(new GameWinnerEntry(gameState, winner));
    }

    public void onPhaseChanged(GameState state, GamePhase newPhase) {
        this.entries.add(new PhaseChangeEntry(state, newPhase));
    }

    public void clear() {
        this.entries.clear();
    }
}

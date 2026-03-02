package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.pot.Pot;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class History {

    @Getter
    private final List<HistoryEntry<?>> entries = new ArrayList<>();

    public void addAction(GameState gameState, Action action) {
        HistoryEntry<?> entry = new ActionEntry(gameState, action);
        logHistoryEntryMessage(entry);
        entries.add(entry);
    }

    public void addFlowStep(GameState gameState, ActionStepType step) {
        HistoryEntry<?> entry = new ActionStepEntry(gameState, step);
        logHistoryEntryMessage(entry);
        entries.add(entry);
    }

    public void addPotWinner(GameState gameState, Pot pot, List<Player> winners, boolean showdown) {
        HistoryEntry<?> entry = new PotWinnerEntry(gameState, new PotWinnerEntry.Context(pot, winners, showdown));
        logHistoryEntryMessage(entry);
        entries.add(entry);
    }

    public void addGameWinner(GameState gameState, Player winner) {
        HistoryEntry<?> entry = new GameWinnerEntry(gameState, winner);
        logHistoryEntryMessage(entry);
        entries.add(entry);
    }

    public void onPhaseChanged(GameState state, GamePhase newPhase) {
        HistoryEntry<?> entry = new PhaseChangeEntry(state, newPhase);
        logHistoryEntryMessage(entry);
        this.entries.add(entry);
    }

    private void logHistoryEntryMessage(HistoryEntry<?> historyEntry) {
        String category = historyEntry.getEntryCategory();
        List<String> properties = historyEntry.getProperties().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
                .toList();

        String message = String.format("History entry: %s [%s]", category, String.join(", ", properties));
        log.debug(message);
    }
}

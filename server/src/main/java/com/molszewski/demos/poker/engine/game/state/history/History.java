package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.action.AllIn;
import com.molszewski.demos.poker.engine.game.state.action.Raise;
import com.molszewski.demos.poker.engine.hand.HandType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class History {

    private final List<HistoryEntry> entries = new ArrayList<>();
    private GamePhase previousPhase = GamePhase.NOT_STARTED;
    @Getter
    private boolean betPlacedInCurrentPhase = false;

    public void addAction(Action action) {
        entries.add(new ActionEntry(action));
        if (action instanceof Raise || action instanceof AllIn) {
            this.betPlacedInCurrentPhase = true;
        }
    }

    public void addFlowStep(FlowStep step) {
        entries.add(new FlowStepEntry(step));
    }

    public void onPhaseChanged(GamePhase phase, Optional<WinnerData> winnerData) {
        if (phase.equals(this.previousPhase)) {
            return;
        }

        this.previousPhase = phase;
        this.entries.add(new PhaseChangeEntry(phase));

        if (phase.equals(GamePhase.SHOWDOWN)) {
            winnerData
                    .filter(winner -> winner.handType() != null)
                    .ifPresent(winner -> this.entries.add(new WinnerEntry(winner.playerId(), winner.handType())));
        } else if (phase.equals(GamePhase.FINISHED)) {
            winnerData.ifPresent(winner -> this.entries.add(new GameWinnerEntry(winner.playerId())));
        }

        this.betPlacedInCurrentPhase = false;
    }

    public List<HistoryEntry> getSortedEntries() {
        return entries.stream().sorted(Comparator.comparingLong(HistoryEntry::getTs)).toList();
    }

    public record WinnerData(String playerId, HandType handType) {
    }
}

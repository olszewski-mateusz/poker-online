package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.action.AllIn;
import com.molszewski.demos.poker.engine.game.state.action.Raise;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class History {

    @Getter
    private final List<HistoryEntry> entries = new ArrayList<>();

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

    public void onPhaseChanged(GameState state, GamePhase newPhase) {

        GamePhase previousPhase = state.getGamePhase();
        if (newPhase.equals(previousPhase)) {
            return;
        }

        this.entries.add(new PhaseChangeEntry(newPhase));

        if (newPhase.equals(GamePhase.SHOWDOWN)) {
            state.getWinner()
                    .filter(winner -> winner.getHand().getHandType() != null)
                    .ifPresent(winner -> this.entries.add(new WinnerEntry(winner.getId(), winner.getHand().getHandType())));
        } else if (newPhase.equals(GamePhase.FINISHED)) {
            state.getWinner().ifPresent(winner -> this.entries.add(new GameWinnerEntry(winner.getId())));
        }

        this.betPlacedInCurrentPhase = false;
    }
}

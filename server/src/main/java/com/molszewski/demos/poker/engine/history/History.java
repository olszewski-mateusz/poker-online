package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.pot.PotType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class History {

    @Getter
    private final List<HistoryEntry> entries = new ArrayList<>();

    public void addAction(Action action) {
        entries.add(new ActionEntry(action));
    }

    public void addFlowStep(FlowStep step) {
        entries.add(new FlowStepEntry(step));
    }

    public void addPotWinner(int potAmount, PotType potType, Set<String> winnerIds, boolean showdown) {
        entries.add(new PotWinnerEntry(potAmount, potType, winnerIds, showdown));
    }

    public void addWinner(String playerId) {
        entries.add(new WinnerEntry(playerId));
    }

    public void onPhaseChanged(GameState state, GamePhase newPhase) {

        GamePhase previousPhase = state.getGamePhase();
        if (newPhase.equals(previousPhase)) {
            return;
        }

        this.entries.add(new PhaseChangeEntry(newPhase));

        if (newPhase.equals(GamePhase.FINISHED)) {
            state.getPlayers().stream()
                    .filter(player -> !player.isFolded())
                    .findFirst()
                    .ifPresent(winner -> this.entries.add(new GameWinnerEntry(winner.getId())));
        }
    }
}

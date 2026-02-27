package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.action.AllIn;
import com.molszewski.demos.poker.engine.game.state.action.Bet;
import com.molszewski.demos.poker.engine.game.state.action.Raise;
import com.molszewski.demos.poker.engine.hand.HandType;
import com.molszewski.demos.poker.engine.card.Card;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class History {

    @Getter
    private final List<HistoryEntry> entries = new ArrayList<>();

    @Getter
    private boolean betPlacedInCurrentPhase = false;
    @Getter
    private int lastFullRaiseSizeInCurrentPhase = 0;

    public void addAction(Action action) {
        entries.add(new ActionEntry(action));
        if (action instanceof Raise || action instanceof AllIn || action instanceof Bet) {
            this.betPlacedInCurrentPhase = true;
        }
    }

    public void addFlowStep(FlowStep step) {
        entries.add(new FlowStepEntry(step));
    }

    public void addPotWinner(int potIndex, int potAmount, List<String> winnerIds) {
        entries.add(new PotWinnerEntry(potIndex, potAmount, winnerIds));
    }

    public void addWinner(String playerId, HandType handType, List<Card> keyCards) {
        entries.add(new WinnerEntry(playerId, handType, keyCards));
    }

    public void addShowdownSummary(
            List<ShowdownSummaryEntry.PotSummary> pots,
            List<ShowdownSummaryEntry.PlayerSummary> winners
    ) {
        entries.add(new ShowdownSummaryEntry(pots, winners));
    }

    public void registerFullRaiseSize(int raiseSize) {
        if (raiseSize <= 0) {
            throw new IllegalArgumentException("raiseSize must be positive");
        }
        this.betPlacedInCurrentPhase = true;
        this.lastFullRaiseSizeInCurrentPhase = raiseSize;
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

        this.betPlacedInCurrentPhase = false;
        this.lastFullRaiseSizeInCurrentPhase = 0;
    }
}

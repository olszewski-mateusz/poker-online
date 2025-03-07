package com.molszewski.demos.poker.web.collector.history.entry;

import com.molszewski.demos.poker.core.game.state.GamePhase;
import lombok.Getter;

@Getter
public class PhaseChangeEntry extends HistoryEntry{
    private final GamePhase details;

    protected PhaseChangeEntry(GamePhase gamePhase) {
        super("PHASE_CHANGE");
        this.details = gamePhase;
    }

    public static PhaseChangeEntry fromPhase(GamePhase phase) {
        return new PhaseChangeEntry(phase);
    }
}

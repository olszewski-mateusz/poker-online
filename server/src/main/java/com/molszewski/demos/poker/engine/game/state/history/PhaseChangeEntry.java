package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.game.state.GamePhase;
import lombok.Getter;

@Getter
public class PhaseChangeEntry extends HistoryEntry {
    private final GamePhase phase;

    public PhaseChangeEntry(GamePhase phase) {
        this.phase = phase;
    }
}

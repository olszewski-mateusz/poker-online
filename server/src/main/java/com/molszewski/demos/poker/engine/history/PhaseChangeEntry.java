package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.game.GamePhase;
import lombok.Getter;

@Getter
public class PhaseChangeEntry extends HistoryEntry {
    private final GamePhase phase;

    public PhaseChangeEntry(GamePhase phase) {
        this.phase = phase;
    }
}

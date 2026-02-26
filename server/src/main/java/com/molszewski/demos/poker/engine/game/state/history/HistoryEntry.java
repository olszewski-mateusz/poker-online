package com.molszewski.demos.poker.engine.game.state.history;

import lombok.Getter;

@Getter
public abstract class HistoryEntry {
    private final long ts;

    protected HistoryEntry() {
        this.ts = System.currentTimeMillis();
    }
}

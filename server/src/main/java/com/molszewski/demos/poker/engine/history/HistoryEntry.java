package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.game.GameState;
import lombok.Getter;

import java.util.Map;

@Getter
public abstract class HistoryEntry<T> {

    private final long ts;
    private final Map<String, String> properties;

    protected HistoryEntry(GameState gameState, T context) {
        this.ts = System.currentTimeMillis();
        properties = buildProperties(gameState, context);
    }

    public abstract String getEntryCategory();

    protected abstract Map<String, String> buildProperties(GameState gameState, T context);
}

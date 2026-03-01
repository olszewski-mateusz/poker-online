package com.molszewski.demos.poker.engine.history;

import lombok.Getter;

@Getter
public class WinnerEntry extends HistoryEntry {
    private final String playerId;

    public WinnerEntry(String playerId) {
        this.playerId = playerId;
    }
}

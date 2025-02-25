package com.molszewski.demos.poker.web.collector.history.entry;

import lombok.Getter;

@Getter
public abstract class HistoryEntry {
    private final String entryType;

    protected HistoryEntry(String entryType) {
        this.entryType = entryType;
    }

    public abstract Object getDetails();
}

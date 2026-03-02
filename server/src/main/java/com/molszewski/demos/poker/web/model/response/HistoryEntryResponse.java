package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.history.HistoryEntry;

import java.util.Map;

public record HistoryEntryResponse(
        String category,
        long ts,
        Map<String, String> properties
) {
    public static HistoryEntryResponse fromHistoryEntry(
            HistoryEntry<?> entry
    ) {
        return new HistoryEntryResponse(
                entry.getEntryCategory(),
                entry.getTs(),
                entry.getProperties()
        );
    }
}

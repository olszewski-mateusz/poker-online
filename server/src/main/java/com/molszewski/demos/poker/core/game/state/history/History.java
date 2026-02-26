package com.molszewski.demos.poker.core.game.state.history;

import com.molszewski.demos.poker.core.game.state.action.Action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class History {

    private final List<HistoryEntry> entries = new ArrayList<>();

    public void addAction(Action action) {
        entries.add(new ActionEntry(action));
    }

    public void addFlowStep(FlowStep step) {
        entries.add(new FlowStepEntry(step));
    }

    public List<HistoryEntry> getSortedEntries() {
        return entries.stream().sorted(Comparator.comparingLong(HistoryEntry::getTs)).toList();
    }
}

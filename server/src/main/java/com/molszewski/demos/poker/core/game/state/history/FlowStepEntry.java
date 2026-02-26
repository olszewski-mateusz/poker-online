package com.molszewski.demos.poker.core.game.state.history;

import lombok.Getter;

@Getter
public class FlowStepEntry extends HistoryEntry {
    private final FlowStep step;

    public FlowStepEntry(FlowStep step) {
        this.step = step;
    }
}

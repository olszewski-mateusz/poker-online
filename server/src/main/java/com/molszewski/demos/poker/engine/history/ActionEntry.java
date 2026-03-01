package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.action.Action;
import lombok.Getter;

@Getter
public class ActionEntry extends HistoryEntry{

    private final Action action;

    public ActionEntry(Action action) {
        this.action = action;
    }
}

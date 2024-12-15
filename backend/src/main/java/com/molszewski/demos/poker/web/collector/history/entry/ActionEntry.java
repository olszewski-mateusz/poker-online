package com.molszewski.demos.poker.web.collector.history.entry;

import com.molszewski.demos.poker.persistence.entity.command.*;
import lombok.Getter;

@Getter
public class ActionEntry extends HistoryEntry {
    private final Details details;

    protected ActionEntry(Type actionType, String playerName, Object details) {
        super(actionType.toString());
        this.details = new Details(playerName, details);
    }

    public static ActionEntry fromCommand(Command command, String playerName) {
        return switch (command) {
            case JoinCommand ignored -> new ActionEntry(Type.JOIN, playerName, null);
            case AllInCommand ignored -> new ActionEntry(Type.ALL_IN, playerName, null);
            case CheckCommand ignored -> new ActionEntry(Type.CHECK, playerName, null);
            case FoldCommand ignored -> new ActionEntry(Type.FOLD, playerName, null);
            case RaiseCommand raiseCommand -> new ActionEntry(Type.RAISE, playerName, raiseCommand.getAmount());
            case ReadyCommand readyCommand -> new ActionEntry(Type.READY, playerName, readyCommand.isReady());
            case ReplaceCommand replaceCommand ->
                    new ActionEntry(Type.REPLACE, playerName, replaceCommand.getCardsToReplace().size());
        };
    }

    public enum Type {
        JOIN, ALL_IN, CHECK, FOLD, RAISE, READY, REPLACE
    }

    public record Details(String playerName, Object value) {
    }
}

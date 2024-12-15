package com.molszewski.demos.poker.web.history;

import com.molszewski.demos.poker.persistence.entity.command.*;
import com.molszewski.demos.poker.persistence.metadata.MetadataCollector;

public record HistoryEntry(
        String playerName,
        Type actionType,
        Integer amount
) {
    public static HistoryEntry fromCommand(Command command, MetadataCollector metadataCollector) {
        String playerName = metadataCollector.getPlayerMetadata(command.getPlayerId()).name();
        return switch (command) {
            case JoinCommand ignored -> new HistoryEntry(playerName, Type.JOIN, null);
            case AllInCommand ignored -> new HistoryEntry(playerName, Type.ALL_IN, null);
            case CheckCommand ignored -> new HistoryEntry(playerName, Type.CHECK, null);
            case FoldCommand ignored -> new HistoryEntry(playerName, Type.FOLD, null);
            case RaiseCommand raiseCommand -> new HistoryEntry(playerName, Type.RAISE, raiseCommand.getAmount());
            case ReadyCommand readyCommand -> new HistoryEntry(playerName, Type.READY, readyCommand.isReady() ? 1 : 0);
            case ReplaceCommand replaceCommand -> new HistoryEntry(playerName, Type.REPLACE, replaceCommand.getCardsToReplace().size());
        };
    }

    public enum Type {
        JOIN, ALL_IN, CHECK, FOLD, RAISE, READY, REPLACE
    }

}

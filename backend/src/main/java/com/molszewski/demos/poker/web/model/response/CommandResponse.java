package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.persistence.entity.command.*;
import com.molszewski.demos.poker.persistence.metadata.MetadataCollector;

public record CommandResponse(
        String playerName,
        Type actionType,
        Integer amount
) {
    public static CommandResponse from(Command command, MetadataCollector metadataCollector) {
        String playerName = metadataCollector.getPlayerMetadata(command.getPlayerId()).name();
        return switch (command) {
            case JoinCommand ignored -> new CommandResponse(playerName, Type.JOIN, null);
            case AllInCommand ignored -> new CommandResponse(playerName, Type.ALL_IN, null);
            case CheckCommand ignored -> new CommandResponse(playerName, Type.CHECK, null);
            case FoldCommand ignored -> new CommandResponse(playerName, Type.FOLD, null);
            case RaiseCommand raiseCommand -> new CommandResponse(playerName, Type.RAISE, raiseCommand.getAmount());
            case ReadyCommand ignored -> new CommandResponse(playerName, Type.READY, null);
            case ReplaceCommand replaceCommand -> new CommandResponse(playerName, Type.REPLACE, replaceCommand.getCardsToReplace().size());
        };
    }

    public enum Type {
        JOIN, ALL_IN, CHECK, FOLD, RAISE, READY, REPLACE
    }

}

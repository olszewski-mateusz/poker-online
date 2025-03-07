package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;

public record FoldRequest(String playerId) {
    public Command toCommand() {
        return Command.fold(playerId);
    }
}

package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;

public record RaiseRequest(String playerId, int amount) {
    public Command toCommand() {
        return Command.raise(playerId, amount);
    }
}

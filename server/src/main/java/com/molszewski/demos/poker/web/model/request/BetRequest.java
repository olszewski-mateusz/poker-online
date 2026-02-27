package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;

public record BetRequest(String playerId, int amount) {
    public Command toCommand() {
        return Command.bet(playerId, amount);
    }
}

package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;

public record JoinRequest(String displayName) {
    public Command toCommand(String playerId) {
        return Command.join(playerId, displayName);
    }
}

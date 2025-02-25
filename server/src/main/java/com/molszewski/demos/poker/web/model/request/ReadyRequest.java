package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;

public record ReadyRequest(String playerId, boolean ready) {
    public Command toCommand() {
        return Command.ready(playerId, ready);
    }
}

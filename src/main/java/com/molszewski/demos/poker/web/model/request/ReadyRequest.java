package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.ReadyCommand;

public record ReadyRequest(String playerId) {
    public Command toCommand() {
        return ReadyCommand.builder()
                .playerId(playerId)
                .build();
    }
}

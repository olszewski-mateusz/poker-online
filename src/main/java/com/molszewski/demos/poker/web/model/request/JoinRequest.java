package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.JoinCommand;

public record JoinRequest(String displayName) {
    public Command toCommand(String playerId) {
        return JoinCommand.builder()
                .playerId(playerId)
                .displayName(displayName)
                .build();
    }
}

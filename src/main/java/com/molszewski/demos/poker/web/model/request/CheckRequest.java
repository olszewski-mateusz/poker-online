package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.CheckCommand;
import com.molszewski.demos.poker.persistence.entity.command.Command;

public record CheckRequest(String playerId) {
    public Command toCommand() {
        return CheckCommand.builder()
                .playerId(playerId)
                .build();
    }
}

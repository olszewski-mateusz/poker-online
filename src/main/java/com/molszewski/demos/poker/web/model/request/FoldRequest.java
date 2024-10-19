package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.FoldCommand;

public record FoldRequest(String playerId) {
    public Command toCommand() {
        return FoldCommand.builder()
                .playerId(playerId)
                .build();
    }
}

package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.RaiseCommand;

public record RaiseRequest(String playerId, int amount) {
    public Command toCommand() {
        return RaiseCommand.builder()
                .playerId(playerId)
                .amount(amount)
                .build();
    }
}

package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        String displayName
) {
    public Command toCommand(String playerId) {
        return Command.join(playerId, displayName);
    }
}

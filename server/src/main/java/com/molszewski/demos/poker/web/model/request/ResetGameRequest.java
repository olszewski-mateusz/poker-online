package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import jakarta.validation.constraints.NotBlank;

public record ResetGameRequest(
        @NotBlank
        String requesterPlayerId,
        @NotBlank
        String adminSecret
) {
    public Command toCommand() {
        return Command.resetGame(requesterPlayerId);
    }
}

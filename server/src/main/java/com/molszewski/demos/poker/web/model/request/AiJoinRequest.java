package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiJoinRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        String displayName,
        @NotBlank
        String adminSecret
) {
    public Command toCommand(String playerId) {
        return Command.aiJoin(playerId, displayName);
    }
}

package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.ReplaceCommand;

import java.util.Set;

public record ReplaceRequest(String playerId, Set<Card> cardsToReplace) {
    public Command toCommand() {
        return ReplaceCommand.builder()
                .playerId(playerId)
                .cardsToReplace(cardsToReplace)
                .build();
    }
}

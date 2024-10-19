package com.molszewski.demos.poker.web.model.request;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.persistence.entity.command.Command;

import java.util.Set;

public record ReplaceRequest(String playerId, Set<Card> cardsToReplace) {
    public Command toCommand() {
        return Command.replace(playerId, cardsToReplace);
    }
}

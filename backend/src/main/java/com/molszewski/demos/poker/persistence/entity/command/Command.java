package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.game.state.action.Action;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public abstract sealed class Command permits AllInCommand, CheckCommand, FoldCommand, JoinCommand, RaiseCommand, ReadyCommand, ReplaceCommand {
    private String playerId;
    private String timestamp = LocalDateTime.now().toString();

    protected Command(String playerId) {
        this.playerId = playerId;
    }

    public abstract Action toAction();

    public static Command allIn(String playerId) {
        return new AllInCommand(playerId);
    }

    public static Command ready(String playerId) {
        return new ReadyCommand(playerId);
    }

    public static Command join(String playerId, String displayName) {
        return new JoinCommand(playerId, displayName);
    }

    public static Command check(String playerId) {
        return new CheckCommand(playerId);
    }

    public static Command fold(String playerId) {
        return new FoldCommand(playerId);
    }

    public static Command replace(String playerId, Set<Card> cardsToReplace) {
        return new ReplaceCommand(playerId, cardsToReplace);
    }

    public static Command raise(String playerId, int amount) {
        return new RaiseCommand(playerId, amount);
    }
}

package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.action.Action;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public abstract sealed class Command permits AllInCommand, BetCommand, CallCommand, CheckCommand, FoldCommand, JoinCommand, AiJoinCommand, RaiseCommand, ReadyCommand, ReplaceCommand {
    private String playerId;
    private String timestamp = LocalDateTime.now().toString();

    protected Command(String playerId) {
        this.playerId = playerId;
    }

    public abstract Action toAction();

    public static Command allIn(String playerId) {
        return new AllInCommand(playerId);
    }

    public static Command ready(String playerId, boolean ready) {
        return new ReadyCommand(playerId, ready);
    }

    public static Command join(String playerId, String displayName) {
        return new JoinCommand(playerId, displayName);
    }

    public static Command aiJoin(String playerId, String displayName) {
        return new AiJoinCommand(playerId, displayName);
    }

    public static Command check(String playerId) {
        return new CheckCommand(playerId);
    }

    public static Command call(String playerId) {
        return new CallCommand(playerId);
    }

    public static Command bet(String playerId, int amount) {
        return new BetCommand(playerId, amount);
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

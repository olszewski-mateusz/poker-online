package com.molszewski.demos.poker.persistence.converter;

import com.molszewski.demos.poker.core.game.state.action.*;
import com.molszewski.demos.poker.persistence.entity.command.*;
import org.springframework.stereotype.Service;

@Service
public class CommandConverter {
    public Action toAction(Command command) {
        return switch (command) {
            case JoinCommand joinCommand -> new Join(joinCommand.getPlayerId());
            case ReadyCommand readyCommand -> new Ready(readyCommand.getPlayerId());
            case CheckCommand checkCommand -> new Check(checkCommand.getPlayerId());
            case RaiseCommand raiseCommand -> new Raise(raiseCommand.getPlayerId(), raiseCommand.getAmount());
            case AllInCommand allInCommand -> new AllIn(allInCommand.getPlayerId());
            case FoldCommand foldCommand -> new Fold(foldCommand.getPlayerId());
            case ReplaceCommand replaceCommand -> new Replace(replaceCommand.getPlayerId(), replaceCommand.getCardsToReplace());
        };
    }
}

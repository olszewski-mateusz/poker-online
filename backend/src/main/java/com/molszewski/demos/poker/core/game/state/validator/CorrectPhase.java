package com.molszewski.demos.poker.core.game.state.validator;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;

import java.util.Set;

public class CorrectPhase implements Validator{

    private final Set<GamePhase> correctPhases;

    private CorrectPhase(Set<GamePhase> correctPhases) {
        this.correctPhases = correctPhases;
    }

    public static CorrectPhase of(GamePhase... correctPhases) {
        return new CorrectPhase(Set.of(correctPhases));
    }

    @Override
    public void validate(Action action, GameState state) throws ActionException {
        if (!correctPhases.contains(state.getGamePhase())) {
            throw new ActionException("Action not legal in state " + state.getGamePhase());
        }
    }
}

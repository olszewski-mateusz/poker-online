package com.molszewski.demos.poker.core.game.state;

import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.transition.Transition;
import com.molszewski.demos.poker.core.game.state.validator.Validator;
import jakarta.annotation.Nullable;

import java.util.List;

public interface StateManager {
    default void executeAction(Action action, GameState gameState, GameConfiguration configuration) throws ActionException {
        for (Validator validator : this.getValidators(action)) {
            validator.validate(action, gameState);
        }

        action.execute(gameState, configuration);

        Transition transition = this.getTransition(action);
        if (transition != null) {
            transition.apply(gameState, configuration);
        }
    }

    List<Validator> getValidators(Action action);

    @Nullable
    Transition getTransition(Action action);
}

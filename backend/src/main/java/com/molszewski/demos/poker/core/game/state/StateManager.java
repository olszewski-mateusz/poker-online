package com.molszewski.demos.poker.core.game.state;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.transition.Transition;
import com.molszewski.demos.poker.core.game.state.validator.Validator;
import com.molszewski.demos.poker.core.player.Player;

import java.util.List;

public interface StateManager {
    default void executeAction(Action action, GameState gameState, GameConfiguration configuration) throws ActionException {
        for (Validator validator : this.getValidators(action)) {
            validator.validate(action, gameState);
        }

        action.execute(gameState, configuration);

        this.getTransition(action).apply(gameState, configuration);
    }

    List<Validator> getValidators(Action action);

    Transition getTransition(Action action);
}

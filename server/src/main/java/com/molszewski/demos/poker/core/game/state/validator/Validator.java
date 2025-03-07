package com.molszewski.demos.poker.core.game.state.validator;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;

public interface Validator {
    void validate(Action action, GameState state) throws ActionException;
}

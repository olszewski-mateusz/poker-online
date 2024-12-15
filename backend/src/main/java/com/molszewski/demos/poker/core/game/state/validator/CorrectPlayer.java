package com.molszewski.demos.poker.core.game.state.validator;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;

class CorrectPlayer implements Validator {

    private CorrectPlayer() {
    }

    public static CorrectPlayer of() {
        return new CorrectPlayer();
    }

    @Override
    public void validate(Action action, GameState state) throws ActionException {
        Player currentPlayer = state.getCurrentPlayer();
        if (currentPlayer.getId().equals(action.getPlayerId())) {
            if (currentPlayer.isReady()) {
                throw new ActionException(String.format("Player %s is ready", currentPlayer.getId()));
            }
            if (currentPlayer.isFolded()) {
                throw new ActionException(String.format("Player %s folded", currentPlayer.getId()));
            }
        } else {
            throw new ActionException("It's not player " + action.getPlayerId() + "turn");
        }
    }
}

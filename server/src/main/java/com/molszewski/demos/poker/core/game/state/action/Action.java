package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import lombok.Getter;

@Getter
public abstract sealed class Action permits Join, Ready, Check, Raise, Fold, Replace, AllIn {
    private final String playerId;

    protected Action(String playerId) {
        this.playerId = playerId;
    }

    public abstract void execute(GameState gameState, GameConfiguration configuration) throws ActionException;
}

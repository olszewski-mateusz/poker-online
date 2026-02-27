package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import lombok.Getter;

@Getter
public abstract sealed class Action permits Join, Ready, Check, Call, Bet, Raise, Fold, Replace, AllIn {
    private final String playerId;

    protected Action(String playerId) {
        this.playerId = playerId;
    }

    public final void apply(GameState gameState, GameConfiguration configuration) throws GameStateException {
        validate(gameState, configuration);
        execute(gameState, configuration);
    }

    protected abstract void validate(GameState gameState, GameConfiguration configuration) throws GameStateException;

    protected abstract void execute(GameState gameState, GameConfiguration configuration);
}

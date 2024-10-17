package com.molszewski.demos.poker.core.game.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;

import java.util.UUID;

public abstract sealed class Action permits Join, Ready, Check, Raise, Fold, Replace {
    private final UUID playerId;

    protected Action(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public abstract void execute(Board board, GameConfiguration configuration) throws ActionException;
}

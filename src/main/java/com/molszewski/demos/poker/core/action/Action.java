package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.Game;

import java.util.UUID;

public abstract class Action {
    protected final UUID playerId;

    protected Action(UUID playerId)  {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public abstract void execute(Game game);
}

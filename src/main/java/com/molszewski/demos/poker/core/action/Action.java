package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.GameContext;
import com.molszewski.demos.poker.core.game.GameException;
import com.molszewski.demos.poker.core.game.GameState;

import java.util.Set;
import java.util.UUID;

public abstract class Action {
    private final UUID playerId;

    protected Action(UUID playerId)  {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }


    public void execute(GameContext gameContext) throws GameException {
        if (!legalStates().contains(gameContext.getGameState())) {
            throw new GameException("Action " + this.getClass().getSimpleName() + "not legal in state " + gameContext.getGameState());
        }
        changeState(gameContext);
    }

    protected abstract Set<GameState> legalStates();

    protected abstract void changeState(GameContext gameContext) throws GameException;
}

package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.GameContext;
import com.molszewski.demos.poker.core.game.GameException;
import com.molszewski.demos.poker.core.game.GameState;

import java.util.Set;
import java.util.UUID;

public class AddPlayer extends Action {

    public AddPlayer(UUID playerId) {
        super(playerId);
    }

    @Override
    protected Set<GameState> legalStates() {
        return Set.of(GameState.NOT_STARTED);
    }

    @Override
    protected void changeState(GameContext gameContext) throws GameException {
        gameContext.addPlayer(this.getPlayerId());
    }
}

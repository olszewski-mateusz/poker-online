package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.exception.GameStateException;

import java.util.Set;

public final class Join extends Action {

    public Join(String playerId) {
        super(playerId);
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws GameStateException {
        gameState.validatePhase(Set.of(GamePhase.NOT_STARTED));
        if (gameState.playerExists(getPlayerId())) {
            throw new GameStateException("Player already in game");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(this);
        gameState.addPlayer(this.getPlayerId(), configuration.startMoney());
    }
}

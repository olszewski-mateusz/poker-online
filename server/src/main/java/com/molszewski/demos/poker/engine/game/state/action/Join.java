package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;

import java.util.Set;

public final class Join extends Action {
    private final String displayName;

    public Join(String playerId, String displayName) {
        super(playerId);
        this.displayName = displayName;
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
        gameState.addPlayer(this.getPlayerId(), this.displayName, configuration.startMoney());
    }
}

package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;

public final class Join extends Action {
    private final String displayName;

    public Join(String playerId, String displayName) {
        super(playerId);
        this.displayName = displayName;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        if (gameState.getPlayerState().exists(getPlayerId())) {
            throw new ActionValidationException("Player already in game");
        }
        if (gameState.getPlayerState().size() >= configuration.maxPlayers()) {
            throw new ActionValidationException("Game is full");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getPlayerState().add(this.getPlayerId(), this.displayName, configuration.startChips());
        gameState.getHistory().addAction(gameState, this);
    }
}

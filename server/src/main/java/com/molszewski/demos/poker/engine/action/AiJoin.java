package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.ai.AiConfiguration;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;

public final class AiJoin extends Action {
    private final String displayName;
    private final AiConfiguration aiConfiguration;

    public AiJoin(String playerId, String displayName, AiConfiguration aiConfiguration) {
        super(playerId);
        this.displayName = displayName;
        this.aiConfiguration = aiConfiguration;
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
        gameState.getAiState().addAi(this.getPlayerId(), this.aiConfiguration);
    }
}

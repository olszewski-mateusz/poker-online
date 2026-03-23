package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;

public final class ResetGame extends Action {
    public ResetGame(String playerId) {
        super(playerId);
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws PlayerNotFound {
        if (!gameState.getPlayerState().exists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getPlayerState().resetForLobby(configuration.startChips());
        gameState.getPotState().resetToNewRound();
        gameState.getDeck().reset();
        gameState.getHistory().clear();
        gameState.setGamePhase(GamePhase.NOT_STARTED);
        gameState.getHistory().addAction(gameState, this);
    }
}

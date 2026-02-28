package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;

public final class Join extends Action {
    private final String displayName;

    public Join(String playerId, String displayName) {
        super(playerId);
        this.displayName = displayName;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        if (gameState.playerExists(getPlayerId())) {
            throw new ActionValidationException("Player already in game");
        }
        if (gameState.getPlayers().size() >= configuration.maxPlayers()) {
            throw new ActionValidationException("Game is full");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(this);
        gameState.getPlayers().add(new Player(this.getPlayerId(), this.displayName, configuration.startChips()));
    }
}

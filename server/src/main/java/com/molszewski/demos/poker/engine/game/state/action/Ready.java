package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.Set;

public final class Ready extends Action {
    private final boolean isReady;

    public Ready(String playerId, boolean isReady) {
        super(playerId);
        this.isReady = isReady;
    }

    public boolean isReady() {
        return isReady;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        gameState.validatePhase(Set.of(GamePhase.NOT_STARTED, GamePhase.SHOWDOWN, GamePhase.FOLDOUT, GamePhase.FINISHED));
        if (!gameState.playerExists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }

        Player player = gameState.getPlayerById(this.getPlayerId());
        if (isReady && player.isReady()) {
            throw new ActionValidationException(String.format("Player %s is already ready", player.getId()));
        }
        if (!isReady && !player.isReady()) {
            throw new ActionValidationException(String.format("Player %s is already not ready", player.getId()));
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration)  {
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());
        player.setReady(isReady);
        ActionSteps.tryStartRound(gameState, configuration);
    }
}

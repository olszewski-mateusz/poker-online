package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.exception.GameStateException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

import java.util.Set;

public final class Ready extends Action {
    private final boolean isReady;

    public Ready(String playerId, boolean isReady) {
        super(playerId);
        this.isReady = isReady;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws GameStateException {
        gameState.validatePhase(Set.of(GamePhase.NOT_STARTED, GamePhase.SHOWDOWN));
        if (!gameState.playerExists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }

        Player player = gameState.getPlayerById(this.getPlayerId());
        if (isReady && player.isReady()) {
            throw new GameStateException(String.format("Player %s is already ready", player.getId()));
        }
        if (!isReady && !player.isReady()) {
            throw new GameStateException(String.format("Player %s is already not ready", player.getId()));
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration)  {
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());
        player.setReady(isReady);
        ActionFlow.tryStartRound(gameState, configuration);
    }
}

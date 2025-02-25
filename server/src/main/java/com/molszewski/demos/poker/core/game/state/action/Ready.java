package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

public final class Ready extends Action {
    private final boolean ready;

    public Ready(String playerId, boolean ready) {
        super(playerId);
        this.ready = ready;
    }

    @Override
    public void execute(GameState gameState, GameConfiguration configuration) throws ActionException {
        Player player = gameState.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));
        if (ready && player.isReady()) {
            throw new ActionException(String.format("Player %s is already ready", player.getId()));
        }
        if (!ready && !player.isReady()) {
            throw new ActionException(String.format("Player %s is already not ready", player.getId()));
        }
        player.setReady(ready);
    }
}

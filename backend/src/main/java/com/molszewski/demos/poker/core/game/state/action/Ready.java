package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

public final class Ready extends Action {

    public Ready(String playerId) {
        super(playerId);
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        Player player = board.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));
        if (player.isReady()) {
            throw new ActionException(String.format("Player %s is already ready", player.getId()));
        }
        player.setReady(true);
    }
}

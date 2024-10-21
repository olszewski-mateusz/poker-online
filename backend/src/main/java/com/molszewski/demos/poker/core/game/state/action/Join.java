package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;

public final class Join extends Action {

    public Join(String playerId) {
        super(playerId);
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        if (board.getPlayers().stream().anyMatch(player -> player.getId().equals(this.getPlayerId()))) {
            throw new ActionException("Player already in game");
        }
        board.addPlayer(this.getPlayerId(), configuration.startMoney());
    }
}

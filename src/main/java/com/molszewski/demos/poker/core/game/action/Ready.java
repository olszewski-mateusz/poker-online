package com.molszewski.demos.poker.core.game.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;

import java.util.UUID;

public final class Ready extends Action {

    public Ready(UUID playerId) {
        super(playerId);
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        board.getPlayerById(this.getPlayerId()).setReady(true);
    }
}

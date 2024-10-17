package com.molszewski.demos.poker.core.game.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.player.Player;

import java.util.UUID;

public final class Fold extends Action {

    public Fold(UUID playerId) {
        super(playerId);
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        Player player = board.getPlayerById(getPlayerId());
        player.setReady(true);
        player.setFolded(true);
    }
}

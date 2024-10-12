package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameState;

import java.util.UUID;

public class PlayerReady extends Action {

    private static int MIN_PLAYERS_TO_START = 3;

    protected PlayerReady(UUID playerId) {
        super(playerId);
    }

    @Override
    public void execute(Game game) {
        game.getPlayerById(playerId).setReady(true);
        if (game.playerCount() >= MIN_PLAYERS_TO_START) {
            game.changeState(GameState.STARTED);
        }
    }
}

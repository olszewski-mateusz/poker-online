package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.Game;

import java.util.UUID;

public class AddPlayer extends Action {

    public AddPlayer(UUID playerId) {
        super(playerId);
    }

    @Override
    public void execute(Game game) {
        game.addPlayer(playerId);
    }
}

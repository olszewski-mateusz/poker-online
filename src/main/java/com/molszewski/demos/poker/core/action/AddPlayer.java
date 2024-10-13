package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.GameContext;

import java.util.UUID;

public class AddPlayer extends Action {

    public AddPlayer(UUID playerId) {
        super(playerId);
    }

    @Override
    public void execute(GameContext gameContext) {
        gameContext.addPlayer(playerId);
    }
}

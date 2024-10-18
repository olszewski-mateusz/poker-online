package com.molszewski.demos.poker.core.game.state.exception;

public class PlayerNotFound extends ActionException {
    public PlayerNotFound(String playerId) {
        super(String.format("Player with id %s not found", playerId));
    }
}

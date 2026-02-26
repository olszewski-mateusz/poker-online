package com.molszewski.demos.poker.engine.game.state.exception;

public class PlayerNotFound extends GameStateException {
    public PlayerNotFound(String playerId) {
        super(String.format("Player with id %s not found", playerId));
    }
}

package com.molszewski.demos.poker.engine.exception;

public class PlayerNotFound extends ActionValidationException {
    public PlayerNotFound(String playerId) {
        super(String.format("Player with id %s not found", playerId));
    }
}

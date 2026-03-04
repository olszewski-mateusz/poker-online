package com.molszewski.demos.poker.test;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;

import java.lang.reflect.Field;

public final class TestPlayerState {
    private static final Field CURRENT_PLAYER_FIELD;

    static {
        try {
            CURRENT_PLAYER_FIELD = PlayerState.class.getDeclaredField("currentPlayer");
            CURRENT_PLAYER_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("PlayerState.currentPlayer field not found", e);
        }
    }

    private TestPlayerState() {
    }

    public static void setCurrentPlayer(GameState gameState, Player player) {
        try {
            CURRENT_PLAYER_FIELD.set(gameState.getPlayerState(), player);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set current player for test", e);
        }
    }
}

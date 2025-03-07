package com.molszewski.demos.poker.core.game;

public record GameConfiguration(
        int minPlayersToStart,
        int startMoney,
        int ante
) {
    public static GameConfiguration defaultConfiguration() {
        return new GameConfiguration(2, 1000, 5);
    }
}

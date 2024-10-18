package com.molszewski.demos.poker.core.game;

public record GameConfiguration(
        int minPlayersToStart,
        int startMoney,
        int firstBet
) {
    public static GameConfiguration defaultConfiguration() {
        return new GameConfiguration(3, 1000, 5);
    }
}

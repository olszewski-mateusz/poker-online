package com.molszewski.demos.poker.core.game;

public record GameConfiguration(
        int minPlayersToStartGame,
        int startMoney,
        int minBet
) {
    public static GameConfiguration defaultConfiguration() {
        return new GameConfiguration(3, 1000, 5);
    }
}

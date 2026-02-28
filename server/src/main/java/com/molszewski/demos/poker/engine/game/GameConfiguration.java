package com.molszewski.demos.poker.engine.game;

public record GameConfiguration(
        int minPlayersToStart,
        int startChips,
        int ante,
        int minOpenBet
) {
    private static final int MAX_PLAYERS = 6;
    private static final int DEFAULT_START_CHIPS = 300;
    private static final int DEFAULT_ANTE = 5;
    private static final int DEFAULT_MIN_OPEN_BET = 10;

    public GameConfiguration(int minPlayersToStart, int startChips, int ante) {
        this(
                minPlayersToStart,
                startChips,
                ante,
                DEFAULT_MIN_OPEN_BET
        );
    }

    public GameConfiguration {
        if (minOpenBet == 0) {
            minOpenBet = DEFAULT_MIN_OPEN_BET;
        }
        if (minPlayersToStart < 2) {
            throw new IllegalArgumentException("minPlayersToStart must be at least 2");
        }
        if (minPlayersToStart > MAX_PLAYERS) {
            throw new IllegalArgumentException("minPlayersToStart cannot exceed " + MAX_PLAYERS);
        }
        if (startChips <= 0) {
            throw new IllegalArgumentException("startChips must be positive");
        }
        if (ante <= 0) {
            throw new IllegalArgumentException("ante cannot be zero or negative");
        }
        if (ante > startChips) {
            throw new IllegalArgumentException("ante cannot exceed startChips");
        }
        if (minOpenBet < 1) {
            throw new IllegalArgumentException("minOpenBet must be at least 1");
        }
        if (minOpenBet > startChips) {
            throw new IllegalArgumentException("minOpenBet cannot exceed startChips");
        }
    }

    public int maxPlayers() {
        return MAX_PLAYERS;
    }

    public static GameConfiguration defaultConfiguration() {
        return new GameConfiguration(
                2,
                DEFAULT_START_CHIPS,
                DEFAULT_ANTE,
                DEFAULT_MIN_OPEN_BET

        );
    }
}

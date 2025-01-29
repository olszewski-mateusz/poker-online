package com.molszewski.demos.poker.web.collector.history.entry;

import lombok.Getter;

@Getter
public class GameWinnerEntry extends HistoryEntry {
    private final Details details;

    protected GameWinnerEntry(String playerName) {
        super("GAME_WINNER");
        this.details = new Details(playerName);
    }

    public record Details(String playerName) {
    }

    public static GameWinnerEntry create(String playerName) {
        return new GameWinnerEntry(playerName);
    }
}

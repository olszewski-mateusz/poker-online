package com.molszewski.demos.poker.engine.game.state.history;

import lombok.Getter;

@Getter
public class GameWinnerEntry extends HistoryEntry {
    private final String playerId;

    public GameWinnerEntry(String playerId) {
        this.playerId = playerId;
    }
}

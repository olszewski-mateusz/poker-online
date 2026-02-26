package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.hand.HandType;
import lombok.Getter;

@Getter
public class WinnerEntry extends HistoryEntry {
    private final String playerId;
    private final HandType handType;

    public WinnerEntry(String playerId, HandType handType) {
        this.playerId = playerId;
        this.handType = handType;
    }
}

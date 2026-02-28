package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.hand.HandType;
import lombok.Getter;

import java.util.List;

@Getter
public class WinnerEntry extends HistoryEntry {
    private final String playerId;

    public WinnerEntry(String playerId) {
        this.playerId = playerId;
    }
}

package com.molszewski.demos.poker.engine.game.state.history;

import lombok.Getter;

import java.util.List;

@Getter
public class PotWinnerEntry extends HistoryEntry {
    private final int potIndex;
    private final int potAmount;
    private final List<String> winnerIds;

    public PotWinnerEntry(int potIndex, int potAmount, List<String> winnerIds) {
        this.potIndex = potIndex;
        this.potAmount = potAmount;
        this.winnerIds = List.copyOf(winnerIds);
    }
}

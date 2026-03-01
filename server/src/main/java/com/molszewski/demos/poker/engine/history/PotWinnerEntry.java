package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.pot.PotType;
import lombok.Getter;

import java.util.Set;

@Getter
public class PotWinnerEntry extends HistoryEntry {
    private final PotType potType;
    private final int potAmount;
    private final Set<String> winnerIds;
    private final boolean showdown;

    public PotWinnerEntry(int potAmount, PotType potType, Set<String> winnerIds, boolean showdown) {
        this.potAmount = potAmount;
        this.winnerIds = Set.copyOf(winnerIds);
        this.potType = potType;
        this.showdown = showdown;
    }
}

package com.molszewski.demos.poker.web.collector.history.entry;

import com.molszewski.demos.poker.core.hand.HandType;
import lombok.Getter;

@Getter
public class WinnerEntry extends HistoryEntry {
    private final Details details;

    protected WinnerEntry(String playerName, HandType handType) {
        super("WINNER");
        this.details = new Details(playerName, handType);
    }

    public record Details(String playerName, HandType handType) {
    }

    public static WinnerEntry create(String playerName, HandType handType) {
        return new WinnerEntry(playerName, handType);
    }
}

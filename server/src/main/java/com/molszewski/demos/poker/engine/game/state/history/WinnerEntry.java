package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.hand.HandType;
import lombok.Getter;

import java.util.List;

@Getter
public class WinnerEntry extends HistoryEntry {
    private final String playerId;
    private final HandType handType;
    private final List<Card> keyCards;

    public WinnerEntry(String playerId, HandType handType, List<Card> keyCards) {
        this.playerId = playerId;
        this.handType = handType;
        this.keyCards = List.copyOf(keyCards);
    }
}

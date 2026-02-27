package com.molszewski.demos.poker.engine.game.state.history;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.hand.HandType;

import java.util.List;

public class ShowdownSummaryEntry extends HistoryEntry {
    private final List<PotSummary> pots;
    private final List<PlayerSummary> winners;

    public ShowdownSummaryEntry(List<PotSummary> pots, List<PlayerSummary> winners) {
        this.pots = List.copyOf(pots);
        this.winners = List.copyOf(winners);
    }

    public List<PotSummary> getPots() {
        return pots;
    }

    public List<PlayerSummary> getWinners() {
        return winners;
    }

    public record PotSummary(int potIndex, int potAmount, List<String> winnerIds) {
        public PotSummary {
            winnerIds = List.copyOf(winnerIds);
        }
    }

    public record PlayerSummary(String playerId, HandType handType, List<Card> keyCards) {
        public PlayerSummary {
            keyCards = List.copyOf(keyCards);
        }
    }
}

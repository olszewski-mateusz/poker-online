package com.molszewski.demos.poker.engine.hand;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Hand implements Comparable<Hand> {
    private final Set<Card> cards;
    private final HandResult handResult;

    public Hand(Set<Card> handCards) {
        if (handCards.size() != 5) {
            throw new IllegalArgumentException("handCards must have size = 5");
        }
        cards = Set.copyOf(handCards);
        handResult = new HandResult(handCards);
    }

    public HandType getHandType() {
        return handResult.getHandType();
    }

    public Set<Card> getCards() {
        return Set.copyOf(cards);
    }

    public List<Card> getKeyCards() {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        return cards.stream()
                .sorted(Comparator
                        .comparing((Card card) -> rankCounts.get(card.rank()))
                        .reversed()
                        .thenComparing(card -> card.rank().getValue(), Comparator.reverseOrder())
                        .thenComparing(card -> card.suit().ordinal(), Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public int compareTo(Hand otherHand) {
        return handResult.compareTo(otherHand.handResult);
    }
}

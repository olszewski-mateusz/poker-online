package com.molszewski.demos.poker.core.hand;

import com.molszewski.demos.poker.core.card.Card;

import java.util.Set;

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

    @Override
    public int compareTo(Hand otherHand) {
        return handResult.compareTo(otherHand.handResult);
    }
}

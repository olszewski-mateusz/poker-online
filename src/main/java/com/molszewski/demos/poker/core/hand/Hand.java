package com.molszewski.demos.poker.core.hand;

import com.molszewski.demos.poker.core.card.Card;

import java.util.List;

public class Hand implements Comparable<Hand> {
    private final List<Card> cards;
    private final HandResult handResult;

    public Hand(List<Card> handCards) {
        cards = List.copyOf(handCards);
        handResult = new HandResult(handCards);
    }

    public HandType getHandType() {
        return handResult.getHandType();
    }

    @Override
    public int compareTo(Hand otherHand) {
        return handResult.compareTo(otherHand.handResult);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hand hand)) return false;

        return handResult.equals(hand.handResult);
    }

    @Override
    public int hashCode() {
        return handResult.hashCode();
    }
}

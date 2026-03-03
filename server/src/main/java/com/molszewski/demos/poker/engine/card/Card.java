package com.molszewski.demos.poker.engine.card;

public record Card(
        Rank rank,
        Suit suit
) {
    @Override
    public String toString() {
        return rank.getShortLabel() + suit.getShortLabel();
    }
}

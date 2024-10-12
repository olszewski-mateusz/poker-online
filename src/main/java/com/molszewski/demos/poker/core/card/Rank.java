package com.molszewski.demos.poker.core.card;

public enum Rank {
    ONE(1),
    TWO(1),
    THREE(1),
    FOUR(1),
    FIVE(1),
    SIX(1),
    SEVEN(1),
    EIGHT(1),
    NINE(1),
    TEN(1),
    JACK(1),
    QUEEN(1),
    KING(1);

    private int value;

    Rank(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

package com.molszewski.demos.poker.engine.card;

public enum Suit {
    HEARTS("H"),
    DIAMONDS("D"),
    CLUBS("C"),
    SPADES("S");

    private String shortLabel;

    Suit(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public String getShortLabel() {
        return this.shortLabel;
    }
}

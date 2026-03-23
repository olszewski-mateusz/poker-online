package com.molszewski.demos.poker.engine.player;

import com.molszewski.demos.poker.engine.hand.Hand;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {
    private final String id;
    private final String name;

    private int chips;
    @Setter
    private boolean ready;
    @Setter
    private boolean folded;
    @Setter
    private Hand hand;

    public Player(String id, String name, int chips, boolean ready, boolean folded, Hand hand) {
        this.id = id;
        this.name = name;
        this.chips = chips;
        this.ready = ready;
        this.folded = folded;
        this.hand = hand;
    }

    public void takeChips(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        if (amount > this.chips) {
            throw new IllegalArgumentException("Can't have less than 0 chips");
        }
        this.chips -= amount;
    }

    public int takeAllChips() {
        int amount = this.chips;
        this.chips = 0;
        return amount;
    }

    public void addChips(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        this.chips += amount;
    }

    public Player copy() {
        return new Player(id, name, chips, ready, folded, hand);
    }

    public void resetToLobby(int startChips) {
        this.chips = startChips;
        this.ready = false;
        this.folded = true;
        this.hand = null;
    }

}

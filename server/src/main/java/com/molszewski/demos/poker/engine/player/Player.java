package com.molszewski.demos.poker.engine.player;

import com.molszewski.demos.poker.engine.hand.Hand;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {
    private final String id;
    private final String name;

    private int chips;
    private int bet;
    @Setter
    private boolean ready;
    @Setter
    private boolean folded;
    @Setter
    private Hand hand;

    public Player(String id, String name, int startChips) {
        this(id, name, startChips, 0, false, false, null);
    }

    public Player(String id, String name, int chips, int bet, boolean ready, boolean folded, Hand hand) {
        this.id = id;
        this.name = name;
        this.chips = chips;
        this.bet = bet;
        this.ready = ready;
        this.folded = folded;
        this.hand = hand;
    }

    public void transferChipsToBet(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        if (amount > this.chips) {
            throw new IllegalArgumentException("Can't have less than 0 chips");
        }
        this.chips -= amount;
        this.bet += amount;
    }

    public void addChips(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        this.chips += amount;
    }

    public int clearAndGetBet() {
        int amount = bet;
        bet = 0;
        return amount;
    }

    public Player copy() {
        return new Player(id, name, chips, bet, ready, folded, hand);
    }

}

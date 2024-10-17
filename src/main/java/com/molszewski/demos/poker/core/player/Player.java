package com.molszewski.demos.poker.core.player;

import com.molszewski.demos.poker.core.hand.Hand;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class Player {
    private final UUID id;
    private int money = 0;
    private int bid = 0;
    @Setter
    private boolean ready = false;
    @Setter
    private boolean folded = false;
    @Setter
    private Hand hand;

    public Player(UUID id, int startMoney) {
        this.id = id;
        this.money = startMoney;
    }

    private Player(UUID id, int money, int bid, boolean ready, boolean folded, Hand hand) {
        this.id = id;
        this.money = money;
        this.bid = bid;
        this.ready = ready;
        this.folded = folded;
        this.hand = hand;
    }

    public void moveMoneyToBid(int amount) {
        if (amount < 0) {
            throw new IllegalStateException("Amount can't be negative");
        }
        if (amount > this.money) {
            throw new IllegalStateException("Can't have less than 0 money");
        }
        this.money -= amount;
        this.bid += amount;
    }

    public void addMoney(int amount) {
        if (amount < 0) {
            throw new IllegalStateException("Amount can't be negative");
        }
        this.money += amount;
    }

    public int collectBid() {
        int amount = bid;
        bid = 0;
        return amount;
    }

    public Player copy() {
        return new Player(id, money, bid, ready, folded, hand);
    }

}

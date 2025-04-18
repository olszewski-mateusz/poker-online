package com.molszewski.demos.poker.core.player;

import com.molszewski.demos.poker.core.hand.Hand;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {
    private final String id;

    private int money;
    private int bet = 0;
    @Setter
    private boolean ready = false;
    @Setter
    private boolean folded = false;
    @Setter
    private Hand hand;

    public Player(String id, int startMoney) {
        this.id = id;
        this.money = startMoney;
    }

    public Player(String id, int money, int bet, boolean ready, boolean folded, Hand hand) {
        this.id = id;
        this.money = money;
        this.bet = bet;
        this.ready = ready;
        this.folded = folded;
        this.hand = hand;
    }

    public void transferMoneyToBet(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        if (amount > this.money) {
            throw new IllegalArgumentException("Can't have less than 0 money");
        }
        this.money -= amount;
        this.bet += amount;
    }

    public void addMoney(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        this.money += amount;
    }

    public int clearAndGetBet() {
        int amount = bet;
        bet = 0;
        return amount;
    }

    public Player copy() {
        return new Player(id, money, bet, ready, folded, hand);
    }

}

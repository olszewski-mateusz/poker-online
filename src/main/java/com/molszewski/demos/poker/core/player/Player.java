package com.molszewski.demos.poker.core.player;

import com.molszewski.demos.poker.core.hand.Hand;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Player {
    private final UUID id;
    boolean ready = false;
    private int money;
    private int bid = 0;
    private Hand hand;

    public Player(UUID id, int startMoney) {
        this.id = id;
        this.money = startMoney;
    }

}

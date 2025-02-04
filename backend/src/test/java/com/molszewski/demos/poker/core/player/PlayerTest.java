package com.molszewski.demos.poker.core.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("1", 100, 10, false, false, null);
    }

    @Test
    @DisplayName("Transfer money correctly")
    void transferMoneyTest() {
        int moneyBeforeTransfer = player.getMoney();
        int betBeforeTransfer = player.getBet();
        int transferAmount = 50;

        player.transferMoneyToBet(transferAmount);

        assertEquals(moneyBeforeTransfer - transferAmount, player.getMoney());
        assertEquals(betBeforeTransfer + transferAmount, player.getBet());
    }

    @Test
    @DisplayName("Transfer negative money - throws error")
    void transferMoneyNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> player.transferMoneyToBet(-1));
    }

    @Test
    @DisplayName("Transfer not enough money - throws error")
    void transferMoneyNotEnoughTest() {
        int moneyToTransfer = player.getMoney() + 1;
        assertThrows(IllegalArgumentException.class, () -> player.transferMoneyToBet(moneyToTransfer));
    }

    @Test
    @DisplayName("Add money correctly")
    void addMoneyTest() {
        int moneyBeforeTransfer = player.getMoney();
        int transferAmount = 50;

        player.addMoney(transferAmount);
        assertEquals(moneyBeforeTransfer + transferAmount, player.getMoney());
    }

    @Test
    @DisplayName("Add negative money - throws error")
    void addMoneyNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> player.addMoney(-1));
    }

    @Test
    @DisplayName("Clear and get bet correctly")
    void clearAndGetTest() {
        int betBeforeClear = player.getBet();

        int bet = player.clearAndGetBet();

        assertEquals(betBeforeClear, bet);
        assertEquals(0, player.getBet());
    }

    @Test
    @DisplayName("Copy player correctly")
    void copyTest() {
        Player copy = player.copy();

        assertNotSame(player, copy);
        assertEquals(player.getId(), copy.getId());
        assertEquals(player.getMoney(), copy.getMoney());
        assertEquals(player.getBet(), copy.getBet());
        assertEquals(player.isReady(), copy.isReady());
        assertEquals(player.isFolded(), copy.isFolded());
        assertEquals(player.getHand(), copy.getHand());
    }
}
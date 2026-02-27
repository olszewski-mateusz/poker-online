package com.molszewski.demos.poker.engine.player;

import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("1", "1", 100, 10, false, false, null);
    }

    @Test
    @DisplayName("Transfer chips correctly")
    void transferMoneyTest() {
        int chipsBeforeTransfer = player.getChips();
        int betBeforeTransfer = player.getBet();
        int transferAmount = 50;

        player.transferChipsToBet(transferAmount);

        assertEquals(chipsBeforeTransfer - transferAmount, player.getChips());
        assertEquals(betBeforeTransfer + transferAmount, player.getBet());
    }

    @Test
    @DisplayName("Transfer negative chips - throws error")
    void transferMoneyNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> player.transferChipsToBet(-1));
    }

    @Test
    @DisplayName("Transfer not enough chips - throws error")
    void transferMoneyNotEnoughTest() {
        int chipsToTransfer = player.getChips() + 1;
        assertThrows(IllegalArgumentException.class, () -> player.transferChipsToBet(chipsToTransfer));
    }

    @Test
    @DisplayName("Add chips correctly")
    void addChipsTest() {
        int chipsBeforeTransfer = player.getChips();
        int transferAmount = 50;

        player.addChips(transferAmount);
        assertEquals(chipsBeforeTransfer + transferAmount, player.getChips());
    }

    @Test
    @DisplayName("Add negative chips - throws error")
    void addChipsNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> player.addChips(-1));
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
        assertEquals(player.getName(), copy.getName());
        assertEquals(player.getChips(), copy.getChips());
        assertEquals(player.getBet(), copy.getBet());
        assertEquals(player.isReady(), copy.isReady());
        assertEquals(player.isFolded(), copy.isFolded());
        assertEquals(player.getHand(), copy.getHand());
    }
}


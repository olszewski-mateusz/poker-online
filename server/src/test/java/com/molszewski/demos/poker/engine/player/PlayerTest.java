package com.molszewski.demos.poker.engine.player;

import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
class PlayerTest {

    @Test
    void takeChipsTest() {
        Player player = new Player("1", "Alice", 100, false, false, null);

        player.takeChips(40);

        assertEquals(60, player.getChips());
        assertThrows(IllegalArgumentException.class, () -> player.takeChips(-1));
        assertThrows(IllegalArgumentException.class, () -> player.takeChips(61));
    }

    @Test
    void takeAllChipsTest() {
        Player player = new Player("1", "Alice", 77, false, false, null);

        int amount = player.takeAllChips();

        assertEquals(77, amount);
        assertEquals(0, player.getChips());
    }

    @Test
    void addChipsTest() {
        Player player = new Player("1", "Alice", 20, false, false, null);

        player.addChips(15);

        assertEquals(35, player.getChips());
        assertThrows(IllegalArgumentException.class, () -> player.addChips(-1));
    }

    @Test
    void copyKeepsCoreStateTest() {
        Player player = new Player("1", "Alice", 42, true, true, null);

        Player copy = player.copy();

        assertEquals(player.getId(), copy.getId());
        assertEquals(player.getName(), copy.getName());
        assertEquals(player.getChips(), copy.getChips());
        assertEquals(player.isReady(), copy.isReady());
        assertEquals(player.isFolded(), copy.isFolded());
        assertEquals(player.getHand(), copy.getHand());
    }
}

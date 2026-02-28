package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
class GameConfigurationTest {

    @Test
    void defaultConfigurationTest() {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();

        assertEquals(2, configuration.minPlayersToStart());
        assertEquals(6, configuration.maxPlayers());
        assertEquals(300, configuration.startChips());
        assertEquals(5, configuration.ante());
        assertEquals(10, configuration.minOpenBet());
    }

    @Test
    void threeArgConstructorUsesDefaultMinOpenBet() {
        GameConfiguration configuration = new GameConfiguration(2, 1000, 5);

        assertEquals(10, configuration.minOpenBet());
    }

    @Test
    void zeroMinOpenBetFallsBackToDefault() {
        GameConfiguration configuration = new GameConfiguration(2, 1000, 5, 0);

        assertEquals(10, configuration.minOpenBet());
    }

    @Test
    void invalidMinPlayersToStartTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(1, 1000, 5));
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(7, 1000, 5));
    }

    @Test
    void invalidStackAndAnteBoundsTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, -1));
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 1001));
    }

    @Test
    void invalidMinOpenBetBoundsTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 5, 1001));
    }
}

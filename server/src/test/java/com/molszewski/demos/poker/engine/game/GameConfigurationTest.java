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
        assertEquals(5, configuration.minRaiseIncrement());
        assertEquals(1, configuration.minRaiseToCurrentBetMultiplier());
    }

    @Test
    void threeArgConstructorUsesDefaultBettingConstraints() {
        GameConfiguration configuration = new GameConfiguration(2, 1000, 5);

        assertEquals(10, configuration.minOpenBet());
        assertEquals(5, configuration.minRaiseIncrement());
        assertEquals(1, configuration.minRaiseToCurrentBetMultiplier());
    }

    @Test
    void invalidMinPlayersToStartTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(1, 1000, 5));
    }

    @Test
    void minPlayersToStartCannotExceedMaxPlayersTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(7, 1000, 5));
    }

    @Test
    void invalidStartChipsTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 0, 5));
    }

    @Test
    void invalidAnteTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, -1));
    }

    @Test
    void anteCannotExceedStartChipsTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 1001));
    }

    @Test
    void invalidMinOpenBetTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 5, -1, 1, 1));
    }

    @Test
    void minOpenBetCannotExceedStartChipsTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 5, 1001, 1, 1));
    }

    @Test
    void invalidMinRaiseIncrementTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 5, 1, -1, 1));
    }

    @Test
    void minRaiseIncrementCannotExceedStartChipsTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 5, 1, 1001, 1));
    }

    @Test
    void invalidMinRaiseMultiplierTest() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfiguration(2, 1000, 5, 1, 1, -1));
    }
}

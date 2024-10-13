package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddPlayerTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void onePlayerCorrectlyAdded() {
        Game game = new Game(random, configuration);
        game.applyAction(new AddPlayer(UUID.randomUUID()));

        assertEquals(1, game.getPlayers().size());
        assertEquals(configuration.startMoney(), game.getPlayers().getFirst().getMoney());
    }

    @Test
    void threePlayerCorrectlyAdded() {
        Game game = new Game(random, configuration);
        game.applyAction(new AddPlayer(UUID.randomUUID()));
        game.applyAction(new AddPlayer(UUID.randomUUID()));
        game.applyAction(new AddPlayer(UUID.randomUUID()));

        assertEquals(3, game.getPlayers().size());
    }

    @Test
    void samePlayerAddedTwoTimes() {
        Game game = new Game(random, configuration);
        UUID id = UUID.randomUUID();
        game.applyAction(new AddPlayer(id));
        assertFalse(game.applyAction(new AddPlayer(id)));
    }
}
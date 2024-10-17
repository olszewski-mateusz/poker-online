package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.action.Join;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JoinTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void onePlayerCorrectlyAdded() {
        Game game = new Game(random, configuration);
        game.applyAction(new Join(UUID.randomUUID()));

        assertEquals(1, game.getPlayers().size());
        assertEquals(configuration.startMoney(), game.getPlayers().getFirst().getMoney());
    }

    @Test
    void threePlayerCorrectlyAdded() {
        Game game = new Game(random, configuration);
        game.applyAction(new Join(UUID.randomUUID()));
        game.applyAction(new Join(UUID.randomUUID()));
        game.applyAction(new Join(UUID.randomUUID()));

        assertEquals(3, game.getPlayers().size());
    }

    @Test
    void samePlayerAddedTwoTimes() {
        Game game = new Game(random, configuration);
        UUID id = UUID.randomUUID();
        game.applyAction(new Join(id));
        assertFalse(game.applyAction(new Join(id)));
    }
}
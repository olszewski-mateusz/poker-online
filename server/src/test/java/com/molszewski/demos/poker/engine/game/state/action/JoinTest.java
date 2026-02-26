package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class JoinTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("One player joins")
    void onePlayerCorrectlyAdded() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);

        assertEquals(1, gameState.getPlayers().size());
        Player player = gameState.getPlayers().getFirst();
        assertEquals(configuration.startMoney(), player.getMoney());
        assertNull(player.getHand());
        assertEquals(0, player.getBet());
        assertEquals("1", player.getId());
        assertFalse(player.isReady());
        assertFalse(player.isFolded());
    }

    @Test
    @DisplayName("Three players join")
    void threePlayerCorrectlyAdded() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        assertEquals(3, gameState.getPlayers().size());
    }

    @Test
    @DisplayName("One player added two times - throws error")
    void samePlayerAddedTwoTimes() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        assertThrows(GameStateException.class,
                () -> { new Join("1").apply(gameState, configuration); });
    }
}

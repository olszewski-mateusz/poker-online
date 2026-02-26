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
class CheckTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly checks")
    void simpleCheck() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Check("1").apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(configuration.ante(), player.getBet());
        assertEquals(configuration.startMoney() - configuration.ante(), player.getMoney());
    }

    @Test
    @DisplayName("Player correctly calls")
    void simpleCall() throws GameStateException {
        int raiseMoney = 3 * configuration.ante();

        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("3",true).apply(gameState, configuration);
        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);

        new Raise("1", raiseMoney).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("2", player.getId());
        new Check("2").apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(raiseMoney, player.getBet());
        assertEquals(configuration.startMoney() - raiseMoney, player.getMoney());
    }

    @Test
    @DisplayName("Non existing player checks - throws error")
    void playerNotFound() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("3",true).apply(gameState, configuration);
        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);

        assertThrows(GameStateException.class, () -> { new Check("4").apply(gameState, configuration); });
    }

    @Test
    @DisplayName("Player calls when he doesn't have enough chips - throws error")
    void checkNoMoney() throws GameStateException {
        int raiseMoney = 2 * configuration.startMoney();

        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("3",true).apply(gameState, configuration);
        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);

        gameState.getCurrentPlayer().addMoney(raiseMoney);
        new Raise("1", raiseMoney).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("2", player.getId());
        assertEquals(raiseMoney, gameState.getCurrentBet());
        assertThrows(GameStateException.class, () -> { new Check("2").apply(gameState, configuration); });
    }
}

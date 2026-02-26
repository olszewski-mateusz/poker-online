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
class AllInTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly goes all in")
    void simpleAllIn() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new AllIn("1").apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(configuration.startMoney(), player.getBet());
        assertEquals(0, player.getMoney());
    }

    @Test
    @DisplayName("When player goes all in - others are still ready (when they don't have chips)")
    void allInNotChangingReadyZeroChips() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Raise("1", 2 * configuration.ante()).apply(gameState, configuration);
        assertTrue(player.isReady());
        player.transferMoneyToBet(player.getMoney());
        player.clearAndGetBet();
        new AllIn("2").apply(gameState, configuration);
        assertTrue(player.isReady());
    }

    @Test
    @DisplayName("When player goes all in - others are still ready (when current bet isn't changed)")
    void allInNotChangingReadyNoNewMaxBet() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Raise("1", configuration.startMoney()).apply(gameState, configuration);
        player.addMoney(100);
        assertTrue(player.isReady());
        new AllIn("2").apply(gameState, configuration);
        assertTrue(player.isReady());
    }

    @Test
    @DisplayName("When player goes all in - others are not ready (when they have chips)")
    void allInChangingReady() throws GameStateException {
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
        new AllIn("2").apply(gameState, configuration);
        assertFalse(player.isReady());
    }
}

package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class CallTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly calls")
    void simpleCall() throws GameStateException {
        int betAmount = 3 * configuration.ante();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("3", true).apply(gameState, configuration);
        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);

        new Bet("1", betAmount).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("2", player.getId());
        new Call("2").apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(betAmount, player.getBet());
        assertEquals(configuration.startChips() - betAmount, player.getChips());
    }

    @Test
    @DisplayName("Player calls when no chips to call - throws error")
    void callWhenNoDifferenceThrows() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        assertThrows(GameStateException.class, () -> new Call("1").apply(gameState, configuration));
    }

    @Test
    @DisplayName("Player calls when he doesn't have enough chips - throws error")
    void callNoMoney() throws GameStateException {
        int betAmount = 2 * configuration.startChips();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("3", true).apply(gameState, configuration);
        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);

        gameState.getCurrentPlayer().addChips(betAmount);
        new Bet("1", betAmount).apply(gameState, configuration);

        assertEquals("2", gameState.getCurrentPlayer().getId());
        assertEquals(betAmount, gameState.getCurrentBet());
        assertThrows(GameStateException.class, () -> new Call("2").apply(gameState, configuration));
    }
}

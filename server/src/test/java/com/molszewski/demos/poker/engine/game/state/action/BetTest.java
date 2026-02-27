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
class BetTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly bets")
    void simpleBet() throws GameStateException {
        int betAmount = 100;
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Bet("1", betAmount).apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(betAmount, player.getBet());
        assertEquals(configuration.startChips() - betAmount, player.getChips());
    }

    @Test
    @DisplayName("Player bets too little chips - throws error")
    void betTooLittle() throws GameStateException {
        int betAmount = configuration.ante();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        assertThrows(GameStateException.class, () -> new Bet("1", betAmount).apply(gameState, configuration));
    }

    @Test
    @DisplayName("Player bets below minOpenBet - throws error")
    void betBelowMinOpenBet() throws GameStateException {
        GameConfiguration customConfiguration = new GameConfiguration(2, 1000, 5, 20, 1, 1);
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, customConfiguration);
        new Join("2", "2").apply(gameState, customConfiguration);
        new Join("3", "3").apply(gameState, customConfiguration);

        new Ready("1", true).apply(gameState, customConfiguration);
        new Ready("2", true).apply(gameState, customConfiguration);
        new Ready("3", true).apply(gameState, customConfiguration);

        assertThrows(GameStateException.class, () -> new Bet("1", 10).apply(gameState, customConfiguration));
    }

    @Test
    @DisplayName("Player bets too many chips - throws error")
    void betTooMany() throws GameStateException {
        int betAmount = configuration.startChips() + 1;
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        assertThrows(GameStateException.class, () -> new Bet("1", betAmount).apply(gameState, configuration));
    }

    @Test
    @DisplayName("When player bets - others are not ready (when they have chips)")
    void betChangesReadyStatus() throws GameStateException {
        int betAmount = 2 * configuration.ante();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Check("1").apply(gameState, configuration);
        assertTrue(player.isReady());
        new Bet("2", betAmount).apply(gameState, configuration);
        assertFalse(player.isReady());
    }

    @Test
    @DisplayName("Second bet in one phase - throws error")
    void secondBetThrows() throws GameStateException {
        int firstBetAmount = 2 * configuration.ante();
        int secondBetAmount = 3 * configuration.ante();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        new Bet("1", firstBetAmount).apply(gameState, configuration);
        assertThrows(GameStateException.class, () -> new Bet("2", secondBetAmount).apply(gameState, configuration));
    }
}

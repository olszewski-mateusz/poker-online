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
class RaiseTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("Player correctly raises")
    void simpleRaise() throws GameStateException {
        int betAmount = 2 * configuration.ante();
        int raiseAmount = 4 * configuration.ante();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        new Bet("1", betAmount).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("2", player.getId());
        new Raise("2", raiseAmount).apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(raiseAmount, player.getBet());
        assertEquals(configuration.startChips() - raiseAmount, player.getChips());
    }

    @Test
    @DisplayName("Player raises before any bet in phase - throws error")
    void raiseWithoutBetInPhaseThrows() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        assertThrows(GameStateException.class, () -> new Raise("1", 2 * configuration.ante()).apply(gameState, configuration));
    }

    @Test
    @DisplayName("Player raises too little chips - throws error")
    void raiseTooLittle() throws GameStateException {
        int betAmount = 2 * configuration.ante();
        int raiseAmount = 3 * configuration.ante() - 1;
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        new Bet("1", betAmount).apply(gameState, configuration);
        assertThrows(GameStateException.class, () -> new Raise("2", raiseAmount).apply(gameState, configuration));
    }

    @Test
    @DisplayName("Raise respects minRaiseToCurrentBetMultiplier")
    void raiseTooSmallForMultiplierRule() throws GameStateException {
        GameConfiguration customConfiguration = new GameConfiguration(2, 1000, 5, 1, 1, 2);
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, customConfiguration);
        new Join("2", "2").apply(gameState, customConfiguration);
        new Join("3", "3").apply(gameState, customConfiguration);

        new Ready("1", true).apply(gameState, customConfiguration);
        new Ready("2", true).apply(gameState, customConfiguration);
        new Ready("3", true).apply(gameState, customConfiguration);

        new Bet("1", 10).apply(gameState, customConfiguration);
        assertThrows(GameStateException.class, () -> new Raise("2", 15).apply(gameState, customConfiguration));
        assertDoesNotThrow(() -> new Raise("2", 20).apply(gameState, customConfiguration));
    }

    @Test
    @DisplayName("When player raises - others are not ready (when they have chips)")
    void raiseChangesReadyStatus() throws GameStateException {
        int betAmount = 2 * configuration.ante();
        int raiseAmount = 4 * configuration.ante();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        new Bet("1", betAmount).apply(gameState, configuration);
        assertTrue(player.isReady());
        new Raise("2", raiseAmount).apply(gameState, configuration);
        assertFalse(player.isReady());
    }

    @Test
    @DisplayName("When player raises - others are still ready (when they don't have chips)")
    void raiseNotChangesReadyStatus() throws GameStateException {
        int betAmount = 2 * configuration.ante();
        int raiseAmount = 4 * configuration.ante();
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        new Bet("1", betAmount).apply(gameState, configuration);

        player.transferChipsToBet(player.getChips());
        player.clearAndGetBet();
        new Raise("2", raiseAmount).apply(gameState, configuration);
        assertTrue(player.isReady());
    }

    @Test
    @DisplayName("Raise must be at least size of last full raise")
    void raiseMustMatchLastFullRaiseSize() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        new Bet("1", 10).apply(gameState, configuration);
        new Raise("2", 30).apply(gameState, configuration);

        assertThrows(GameStateException.class, () -> new Raise("3", 45).apply(gameState, configuration));
        assertDoesNotThrow(() -> new Raise("3", 50).apply(gameState, configuration));
    }

    @Test
    @DisplayName("minimumRaiseTo handles integer overflow safely")
    void minimumRaiseToOverflowSafety() {
        GameConfiguration customConfiguration = new GameConfiguration(2, 1000, 5, 1, 1, Integer.MAX_VALUE);
        GameState gameState = new GameState(new Deck(random));
        int minimumRaiseTo = Raise.minimumRaiseTo(gameState, customConfiguration, 1_500_000_000);

        assertEquals(Integer.MAX_VALUE, minimumRaiseTo);
    }
}

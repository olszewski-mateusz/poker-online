package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
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

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new AllIn("1").apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(configuration.startChips(), player.getBet());
        assertEquals(0, player.getChips());
    }

    @Test
    @DisplayName("When player goes all in - others are still ready (when they don't have chips)")
    void allInNotChangingReadyZeroChips() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Bet("1", 2 * configuration.ante()).apply(gameState, configuration);
        assertTrue(player.isReady());
        player.transferChipsToBet(player.getChips());
        player.clearAndGetBet();
        new AllIn("2").apply(gameState, configuration);
        assertTrue(player.isReady());
    }

    @Test
    @DisplayName("When player goes all in - others are still ready (when current bet isn't changed)")
    void allInNotChangingReadyNoNewMaxBet() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Bet("1", configuration.startChips()).apply(gameState, configuration);
        player.addChips(100);
        assertTrue(player.isReady());
        new AllIn("2").apply(gameState, configuration);
        assertTrue(player.isReady());
    }

    @Test
    @DisplayName("When player goes all in - others are not ready (when they have chips)")
    void allInChangingReady() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

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

    @Test
    @DisplayName("Short all in does not reopen betting")
    void shortAllInDoesNotReopenBetting() throws GameStateException {
        Player player1 = new Player("1", "1", 100, 40, false, false, null);
        Player player2 = new Player("2", "2", 100, 40, true, false, null);
        Player player3 = new Player("3", "3", 10, 35, false, false, null);

        GameState gameState = new GameState(new Deck(random), java.util.List.of(player1, player2, player3));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(player3);
        gameState.getHistory().addAction(new Bet("1", 20));
        gameState.getHistory().registerFullRaiseSize(10);

        new AllIn("3").apply(gameState, configuration);

        assertTrue(player2.isReady());
    }

    @Test
    @DisplayName("All in below multiplier raise threshold does not reopen betting")
    void allInBelowMultiplierThresholdDoesNotReopen() throws GameStateException {
        GameConfiguration customConfiguration = new GameConfiguration(2, 1000, 5, 1, 1, 2);
        Player player1 = new Player("1", "1", 100, 40, false, false, null);
        Player player2 = new Player("2", "2", 100, 40, true, false, null);
        Player player3 = new Player("3", "3", 35, 35, false, false, null);

        GameState gameState = new GameState(new Deck(random), java.util.List.of(player1, player2, player3));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(player3);
        gameState.getHistory().addAction(new Bet("1", 20));
        gameState.getHistory().registerFullRaiseSize(10);

        new AllIn("3").apply(gameState, customConfiguration);

        assertTrue(player2.isReady());
    }

    @Test
    @DisplayName("All in at minimum raise increment reopens betting")
    void allInAtMinimumRaiseIncrementReopens() throws GameStateException {
        Player player1 = new Player("1", "1", 100, 40, false, false, null);
        Player player2 = new Player("2", "2", 100, 40, true, false, null);
        Player player3 = new Player("3", "3", 20, 30, false, false, null);

        GameState gameState = new GameState(new Deck(random), java.util.List.of(player1, player2, player3));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(player3);
        gameState.getHistory().addAction(new Bet("1", 20));
        gameState.getHistory().registerFullRaiseSize(10);

        new AllIn("3").apply(gameState, configuration);

        assertFalse(player2.isReady());
    }

    @Test
    @DisplayName("All in at multiplier threshold reopens betting")
    void allInAtMultiplierThresholdReopens() throws GameStateException {
        GameConfiguration customConfiguration = new GameConfiguration(2, 1000, 5, 1, 1, 2);
        Player player1 = new Player("1", "1", 100, 40, false, false, null);
        Player player2 = new Player("2", "2", 100, 40, true, false, null);
        Player player3 = new Player("3", "3", 50, 30, false, false, null);

        GameState gameState = new GameState(new Deck(random), java.util.List.of(player1, player2, player3));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(player3);
        gameState.getHistory().addAction(new Bet("1", 20));
        gameState.getHistory().registerFullRaiseSize(10);

        new AllIn("3").apply(gameState, customConfiguration);

        assertFalse(player2.isReady());
    }
}


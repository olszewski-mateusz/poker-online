package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.GameStateException;
import com.molszewski.demos.poker.core.player.Player;
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
    @DisplayName("Player correctly bets 100 chips")
    void simpleBet() throws GameStateException {
        int raiseMoney = 100;

        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        new Raise("1", raiseMoney).apply(gameState, configuration);
        assertTrue(player.isReady());
        assertEquals(raiseMoney, player.getBet());
        assertEquals(configuration.startMoney() - raiseMoney, player.getMoney());
    }

    @Test
    @DisplayName("Player bets too little chips - throws error")
    void raiseTooLittle() throws GameStateException {
        int raiseMoney = configuration.ante() + 1;

        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        assertThrows(GameStateException.class, () -> { new Raise("1", raiseMoney).apply(gameState, configuration); });
    }

    @Test
    @DisplayName("Player bets too many chips - throws error")
    void raiseTooMany() throws GameStateException {
        int raiseMoney = configuration.startMoney() + 1;

        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        assertThrows(GameStateException.class, () -> { new Raise("1", raiseMoney).apply(gameState, configuration); });
    }

    @Test
    @DisplayName("When player raises - others are not ready (when they have chips)")
    void raiseChangesReadyStatus() throws GameStateException {
        int raiseMoney = 2 * configuration.ante();

        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        assertFalse(player.isReady());
        new Check("1").apply(gameState, configuration);
        assertTrue(player.isReady());
        new Raise("2", raiseMoney).apply(gameState, configuration);
        assertFalse(player.isReady());
    }

    @Test
    @DisplayName("When player raises - others are still ready (when they don't have chips)")
    void raiseNotChangesReadyStatus() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1").apply(gameState, configuration);
        new Join("2").apply(gameState, configuration);
        new Join("3").apply(gameState, configuration);

        new Ready("1",true).apply(gameState, configuration);
        new Ready("2",true).apply(gameState, configuration);
        new Ready("3",true).apply(gameState, configuration);

        Player player = gameState.getCurrentPlayer();
        assertEquals("1", player.getId());
        assertFalse(player.isReady());
        new Check("1").apply(gameState, configuration);
        assertTrue(player.isReady());

        player.transferMoneyToBet(player.getMoney());
        player.clearAndGetBet();
        new Raise("2", configuration.startMoney()).apply(gameState, configuration);
        assertTrue(player.isReady());
    }
}

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
class JoinTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("One player joins")
    void onePlayerCorrectlyAdded() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);

        assertEquals(1, gameState.getPlayers().size());
        Player player = gameState.getPlayers().getFirst();
        assertEquals(configuration.startChips(), player.getChips());
        assertNull(player.getHand());
        assertEquals(0, player.getBet());
        assertEquals("1", player.getId());
        assertEquals("1", player.getName());
        assertFalse(player.isReady());
        assertFalse(player.isFolded());
    }

    @Test
    @DisplayName("One player joins with display name")
    void onePlayerWithDisplayNameAdded() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "Alice").apply(gameState, configuration);

        Player player = gameState.getPlayers().getFirst();
        assertEquals("Alice", player.getName());
    }

    @Test
    @DisplayName("Three players join")
    void threePlayerCorrectlyAdded() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        assertEquals(3, gameState.getPlayers().size());
    }

    @Test
    @DisplayName("Seventh player cannot join full table")
    void seventhPlayerCannotJoin() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        for (int i = 1; i <= configuration.maxPlayers(); i++) {
            new Join(String.valueOf(i), String.valueOf(i)).apply(gameState, configuration);
        }

        assertThrows(GameStateException.class, () -> new Join("7", "7").apply(gameState, configuration));
    }

    @Test
    @DisplayName("One player added two times - throws error")
    void samePlayerAddedTwoTimes() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        assertThrows(GameStateException.class,
                () -> { new Join("1", "1").apply(gameState, configuration); });
    }

    @Test
    @DisplayName("Player joins during active round and waits for next round")
    void playerJoinsDuringActiveRound() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());

        new Join("3", "3").apply(gameState, configuration);
        Player player = gameState.getPlayerById("3");
        assertTrue(player.isFolded());
        assertFalse(player.isReady());
        assertNull(player.getHand());
    }

    @Test
    @DisplayName("Player joins in finished game folded and can ready up")
    void playerJoinsInFinishedGame() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));
        gameState.setGamePhase(GamePhase.FINISHED);

        new Join("3", "3").apply(gameState, configuration);
        Player player = gameState.getPlayerById("3");
        assertTrue(player.isFolded());
        assertFalse(player.isReady());

        assertDoesNotThrow(() -> new Ready("3", true).apply(gameState, configuration));
    }

    @Test
    @DisplayName("Player joins in showdown folded and can ready up")
    void playerJoinsInShowdownGame() throws GameStateException {
        GameState gameState = new GameState(new Deck(random));
        gameState.setGamePhase(GamePhase.SHOWDOWN);

        new Join("3", "3").apply(gameState, configuration);
        Player player = gameState.getPlayerById("3");
        assertTrue(player.isFolded());
        assertFalse(player.isReady());

        assertDoesNotThrow(() -> new Ready("3", true).apply(gameState, configuration));
    }
}


package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.action.Ready;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class JoinTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void playerIsAddedWithStartStackAndLobbyState() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "Alice").apply(gameState, configuration);

        Player player = gameState.getPlayerState().getAll().getFirst();
        assertEquals("1", player.getId());
        assertEquals("Alice", player.getName());
        assertEquals(configuration.startChips(), player.getChips());
        assertFalse(player.isReady());
        assertTrue(player.isFolded());
        assertNull(player.getHand());
    }

    @Test
    void duplicatePlayerIdThrows() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "Alice").apply(gameState, configuration);

        assertThrows(ActionValidationException.class, () -> new Join("1", "Alice").apply(gameState, configuration));
    }

    @Test
    void seventhPlayerCannotJoinTable() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        for (int i = 1; i <= configuration.maxPlayers(); i++) {
            new Join(String.valueOf(i), String.valueOf(i)).apply(gameState, configuration);
        }

        assertThrows(ActionValidationException.class, () -> new Join("7", "7").apply(gameState, configuration));
    }

    @Test
    void playerCanJoinFinishedGameAndReadyUpForNextRound() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        gameState.setGamePhase(GamePhase.FINISHED);

        new Join("3", "3").apply(gameState, configuration);

        assertDoesNotThrow(() -> new Ready("3", true).apply(gameState, configuration));
    }
}

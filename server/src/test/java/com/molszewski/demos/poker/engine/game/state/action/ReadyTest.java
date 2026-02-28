package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class ReadyTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    @DisplayName("1 of 3 players is ready")
    void onePlayerReady() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("2", true).apply(gameState, configuration);

        assertEquals(1, gameState.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    @DisplayName("3 of 4 players are ready")
    void morePlayerReady() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);
        new Join("4", "4").apply(gameState, configuration);

        new Ready("4", true).apply(gameState, configuration);
        new Ready("1", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        assertEquals(3, gameState.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    @DisplayName("Player ready again - throws error")
    void playerReadyTwoTimes() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("2", true).apply(gameState, configuration);
        assertThrows(ActionValidationException.class,
                () -> { new Ready("2", true).apply(gameState, configuration); });
    }

    @Test
    @DisplayName("Player not ready again - throws error")
    void playerNotReady() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        assertThrows(ActionValidationException.class,
                () -> { new Ready("2", false).apply(gameState, configuration); });
    }

    @Test
    @DisplayName("Player is correctly not ready")
    void playerReadyThenNotReady() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("2", true).apply(gameState, configuration);
        new Ready("2", false).apply(gameState, configuration);

        assertFalse(gameState.getPlayerById("2").isReady());
    }

    @Test
    @DisplayName("Unknown player is ready")
    void unknownPlayerReady() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));

        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        assertThrows(PlayerNotFound.class, () -> {
            new Ready("4", true).apply(gameState, configuration);
        });
    }
}


package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.action.AiJoin;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.action.RemovePlayer;
import com.molszewski.demos.poker.engine.ai.AiConfiguration;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.test.TestPlayerState;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class RemovePlayerTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void existingPlayerIsRemoved() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("1", "Alice").apply(gameState, configuration);
        new Join("2", "Bob").apply(gameState, configuration);

        new RemovePlayer("2", 0).apply(gameState, configuration);

        assertEquals(1, gameState.getPlayerState().size());
        assertTrue(gameState.getPlayerState().exists("2"));
        assertFalse(gameState.getPlayerState().exists("1"));
    }

    @Test
    void removedAiPlayerIsDeletedFromAiState() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("host", "Host").apply(gameState, configuration);
        new AiJoin("ai-1", "Bot", new AiConfiguration(AiConfiguration.Aggressivity.MEDIUM)).apply(gameState, configuration);

        new RemovePlayer("host", 1).apply(gameState, configuration);

        assertFalse(gameState.getPlayerState().exists("ai-1"));
        assertFalse(gameState.getAiState().isPlayerAi("ai-1"));
    }

    @Test
    void removingCurrentPlayerMovesTurnToNextEligible() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("1", "One").apply(gameState, configuration);
        new Join("2", "Two").apply(gameState, configuration);
        new Join("3", "Three").apply(gameState, configuration);

        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.getPlayerState().getById("1").setFolded(false);
        gameState.getPlayerState().getById("2").setFolded(false);
        gameState.getPlayerState().getById("3").setFolded(false);
        TestPlayerState.setCurrentPlayer(gameState, gameState.getPlayerState().getById("2"));

        new RemovePlayer("1", 1).apply(gameState, configuration);

        assertEquals("3", gameState.getPlayerState().getCurrentPlayer().getId());
    }

    @Test
    void removingUnknownPlayerThrows() {
        GameState gameState = new GameState(new Deck(random));

        assertThrows(PlayerNotFound.class, () -> new RemovePlayer("missing", 0).apply(gameState, configuration));
    }

    @Test
    void removingSelfThrows() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("1", "Alice").apply(gameState, configuration);
        new Join("2", "Bob").apply(gameState, configuration);

        ActionValidationException exception = assertThrows(ActionValidationException.class,
                () -> new RemovePlayer("1", 0).apply(gameState, configuration));
        assertEquals("Cannot remove yourself", exception.getMessage());
    }
}

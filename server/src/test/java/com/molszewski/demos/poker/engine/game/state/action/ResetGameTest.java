package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.action.AiJoin;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.action.Ready;
import com.molszewski.demos.poker.engine.action.ResetGame;
import com.molszewski.demos.poker.engine.action.ActionType;
import com.molszewski.demos.poker.engine.ai.AiConfiguration;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class ResetGameTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void resetKeepsPlayersAndRestoresLobbyState() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("1", "Alice").apply(gameState, configuration);
        new Join("2", "Bob").apply(gameState, configuration);
        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);

        new ResetGame("1").apply(gameState, configuration);

        List<Player> players = gameState.getPlayerState().getAll();
        assertEquals(2, players.size());
        for (Player player : players) {
            assertEquals(configuration.startChips(), player.getChips());
            assertFalse(player.isReady());
            assertTrue(player.isFolded());
            assertNull(player.getHand());
        }
        assertEquals(GamePhase.NOT_STARTED, gameState.getGamePhase());
        assertEquals(0, gameState.getPotState().getAmount());
        assertTrue(gameState.getPotState().getPlayerContributions().isEmpty());
        assertEquals(52, gameState.getDeck().cardsCount());
        assertEquals(0, gameState.getDeck().discardsCount());
        assertEquals(2, gameState.getHistory().getEntries().size());
        assertEquals(ActionType.RESET_GAME.toString(), gameState.getHistory().getEntries().getLast().getProperties().get("actionType"));
    }

    @Test
    void resetPreservesAiSeats() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("1", "Human").apply(gameState, configuration);
        new AiJoin("2", "Bot", new AiConfiguration(AiConfiguration.Aggressivity.MEDIUM)).apply(gameState, configuration);

        new ResetGame("1").apply(gameState, configuration);

        assertTrue(gameState.getPlayerState().exists("2"));
        assertTrue(gameState.getAiState().isPlayerAi("2"));
    }

    @Test
    void resetByUnknownRequesterThrows() {
        GameState gameState = new GameState(new Deck(random));

        assertThrows(PlayerNotFound.class, () -> new ResetGame("missing").apply(gameState, configuration));
    }
}

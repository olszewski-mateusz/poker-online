package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.Bet;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.action.Raise;
import com.molszewski.demos.poker.engine.action.Ready;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class RaiseTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void playerRaiseUpdatesContributionAndLastFullRaise() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();
        new Bet("1", 20).apply(gameState, configuration);

        Player raiser = gameState.getCurrentPlayer();
        new Raise(raiser.getId(), 40).apply(gameState, configuration);

        assertTrue(raiser.isReady());
        assertEquals(40, gameState.getPotState().getPlayerContribution(raiser));
        assertEquals(configuration.startChips() - 40, raiser.getChips());
        assertEquals(20, gameState.getPotState().getLastFullRaise());
    }

    @Test
    void raiseBeforeAnyBetThrows() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();

        assertThrows(ActionValidationException.class, () -> new Raise("1", 20).apply(gameState, configuration));
    }

    @Test
    void raiseBelowMinimumThrows() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();
        new Bet("1", 20).apply(gameState, configuration);

        assertThrows(ActionValidationException.class, () -> new Raise("2", 39).apply(gameState, configuration));
    }

    @Test
    void raiseDoesNotUnsetReadyPlayersWithoutChips() throws ActionValidationException {
        Player noChips = new Player("1", "1", 0, true, false, null);
        Player raiser = new Player("2", "2", 100, false, false, null);
        Player other = new Player("3", "3", 100, true, false, null);
        GameState gameState = new GameState(new Deck(random), List.of(noChips, raiser, other));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        gameState.setCurrentPlayer(raiser);
        gameState.getPotState().addAnte(raiser, 10);
        gameState.getPotState().addAnte(other, 20);
        gameState.getPotState().addBet(other, 20);

        new Raise("2", 40).apply(gameState, configuration);

        assertTrue(noChips.isReady());
    }

    private GameState startedThreePlayerGame() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);
        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);
        return gameState;
    }
}

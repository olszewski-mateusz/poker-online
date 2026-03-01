package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.Bet;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.action.Ready;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class BetTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void playerBetIncreasesContributionAndConsumesOnlyDeltaFromStack() throws ActionValidationException {
        int betAmount = 100;
        GameState gameState = startedThreePlayerGame();

        Player player = gameState.getCurrentPlayer();
        new Bet(player.getId(), betAmount).apply(gameState, configuration);

        assertTrue(player.isReady());
        assertEquals(betAmount, gameState.getPotState().getPlayerContribution(player));
        assertEquals(configuration.startChips() - betAmount, player.getChips());
    }

    @Test
    void betBelowMinOpenBetThrows() throws ActionValidationException {
        GameConfiguration customConfiguration = new GameConfiguration(2, 1000, 5, 20);
        GameState gameState = startedThreePlayerGame(customConfiguration);

        assertThrows(ActionValidationException.class, () -> new Bet("1", 10).apply(gameState, customConfiguration));
    }

    @Test
    void secondBetInPhaseThrows() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();

        new Bet("1", 20).apply(gameState, configuration);

        assertThrows(ActionValidationException.class, () -> new Bet("2", 30).apply(gameState, configuration));
    }

    @Test
    void betLowerThanCurrentContributionThrows() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();
        Player player = gameState.getPlayerById("1");

        new Bet("1", 50).apply(gameState, configuration);
        gameState.getPotState().resetToNewBettingPhase();
        gameState.setGamePhase(GamePhase.SECOND_BETTING);
        gameState.getPlayers().forEach(p -> p.setReady(false));
        gameState.setCurrentPlayer(player);

        assertThrows(ActionValidationException.class, () -> new Bet("1", 40).apply(gameState, configuration));
    }

    private GameState startedThreePlayerGame() throws ActionValidationException {
        return startedThreePlayerGame(configuration);
    }

    private GameState startedThreePlayerGame(GameConfiguration cfg) throws ActionValidationException {
        GameState gameState = new GameState(new Deck(random));
        new Join("1", "1").apply(gameState, cfg);
        new Join("2", "2").apply(gameState, cfg);
        new Join("3", "3").apply(gameState, cfg);
        new Ready("1", true).apply(gameState, cfg);
        new Ready("2", true).apply(gameState, cfg);
        new Ready("3", true).apply(gameState, cfg);
        return gameState;
    }
}

package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.action.Bet;
import com.molszewski.demos.poker.engine.action.Check;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.action.Ready;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class CheckTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void playerCheckSetsReadyWithoutChangingContribution() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();

        Player player = gameState.getPlayerState().getCurrentPlayer();
        int contributionBefore = gameState.getPotState().getPlayerContribution(player);
        new Check(player.getId()).apply(gameState, configuration);

        assertTrue(player.isReady());
        assertEquals(contributionBefore, gameState.getPotState().getPlayerContribution(player));
    }

    @Test
    void checkWhileBehindCurrentBetThrows() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();
        new Bet("1", 20).apply(gameState, configuration);

        assertThrows(ActionValidationException.class, () -> new Check("2").apply(gameState, configuration));
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

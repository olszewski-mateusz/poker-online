package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class CallTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void playerCallMatchesCurrentContribution() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();
        new Bet("1", 15).apply(gameState, configuration);

        Player caller = gameState.getCurrentPlayer();
        new Call(caller.getId()).apply(gameState, configuration);

        assertTrue(caller.isReady());
        assertEquals(15, gameState.getPotState().getPlayerContribution(caller));
        assertEquals(configuration.startChips() - 15, caller.getChips());
    }

    @Test
    void callWhenAlreadyMatchedThrows() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();

        assertThrows(ActionValidationException.class, () -> new Call("1").apply(gameState, configuration));
    }

    @Test
    void callWithoutEnoughChipsThrows() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();
        gameState.getPlayerById("1").addChips(1000);
        new Bet("1", 500).apply(gameState, configuration);

        assertThrows(ActionValidationException.class, () -> new Call("2").apply(gameState, configuration));
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

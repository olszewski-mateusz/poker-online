package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class StartRoundTest {
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void allReadyPlayersStartRoundAndPostAnte() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(new Random(17L)));
        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());
        assertNotNull(gameState.getPlayerById("1").getHand());
        assertNotNull(gameState.getPlayerById("2").getHand());
        assertNotNull(gameState.getPlayerById("3").getHand());
        assertEquals(configuration.ante(), gameState.getPotState().getPlayerContribution("1"));
        assertEquals(configuration.ante(), gameState.getPotState().getPlayerContribution("2"));
        assertEquals(configuration.ante(), gameState.getPotState().getPlayerContribution("3"));
    }

    @Test
    void shortAntePlayerBecomesReadyAfterRoundStart() {
        Player shortStack = new Player("1", "1", 3, true, true, null);
        Player deepStack = new Player("2", "2", 100, true, true, null);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(shortStack, deepStack));
        gameState.setGamePhase(GamePhase.NOT_STARTED);

        ActionSteps.tryStartRound(gameState, configuration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());
        assertEquals(0, shortStack.getChips());
        assertEquals(3, gameState.getPotState().getPlayerContribution(shortStack));
        assertTrue(shortStack.isReady());
        assertFalse(shortStack.isFolded());
    }

    @Test
    void roundSkipsFirstBettingWhenEveryoneIsAllInFromAnte() {
        Player p1 = new Player("1", "1", 3, true, true, null);
        Player p2 = new Player("2", "2", 3, true, true, null);
        GameState gameState = new GameState(new Deck(new Random(17L)), List.of(p1, p2));
        gameState.setGamePhase(GamePhase.NOT_STARTED);

        ActionSteps.tryStartRound(gameState, configuration);

        assertEquals(GamePhase.DRAWING, gameState.getGamePhase());
        assertNotNull(gameState.getCurrentPlayer());
        assertEquals("1", gameState.getCurrentPlayer().getId());
        assertEquals(3, gameState.getPotState().getPlayerContribution("1"));
        assertEquals(3, gameState.getPotState().getPlayerContribution("2"));
        assertFalse(p1.isReady());
        assertFalse(p2.isReady());
    }

    @Test
    void restartFromShowdownRotatesPlayersAndResetsPotBeforeNewAntes() throws ActionValidationException {
        GameState gameState = new GameState(new Deck(new Random(17L)));
        new Join("1", "1").apply(gameState, configuration);
        new Join("2", "2").apply(gameState, configuration);
        new Join("3", "3").apply(gameState, configuration);

        gameState.setGamePhase(GamePhase.SHOWDOWN);
        gameState.getPotState().addAnte(gameState.getPlayerById("1"), 10);
        gameState.getPotState().addAnte(gameState.getPlayerById("2"), 10);
        gameState.getPotState().addAnte(gameState.getPlayerById("3"), 10);

        new Ready("1", true).apply(gameState, configuration);
        new Ready("2", true).apply(gameState, configuration);
        new Ready("3", true).apply(gameState, configuration);

        assertEquals(GamePhase.FIRST_BETTING, gameState.getGamePhase());
        assertEquals("2", gameState.getPlayers().getFirst().getId());
        assertEquals(configuration.ante(), gameState.getPotState().getPlayerContribution("1"));
        assertEquals(configuration.ante(), gameState.getPotState().getPlayerContribution("2"));
        assertEquals(configuration.ante(), gameState.getPotState().getPlayerContribution("3"));
    }
}

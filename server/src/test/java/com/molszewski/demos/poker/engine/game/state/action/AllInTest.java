package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.AllIn;
import com.molszewski.demos.poker.engine.action.Check;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.action.Ready;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import com.molszewski.demos.poker.test.TestPlayerState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class AllInTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();

    @Test
    void simpleAllInTransfersEntireStackToContribution() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();

        Player player = gameState.getPlayerState().getCurrentPlayer();
        new AllIn(player.getId()).apply(gameState, configuration);

        assertTrue(player.isReady());
        assertEquals(0, player.getChips());
        assertEquals(configuration.startChips(), gameState.getPotState().getPlayerContribution(player));
    }

    @Test
    void allInThatIncreasesMaxContributionResetsOtherReadyPlayers() throws ActionValidationException {
        GameState gameState = startedThreePlayerGame();
        Player first = gameState.getPlayerState().getCurrentPlayer();
        new Check(first.getId()).apply(gameState, configuration);

        new AllIn("2").apply(gameState, configuration);

        assertFalse(first.isReady());
    }

    @Test
    void shortAllInDoesNotResetReadyPlayersWhenMaxDoesNotChange() throws ActionValidationException {
        Player p1 = new Player("1", "1", 100, true, false, null);
        Player p2 = new Player("2", "2", 10, false, false, null);
        Player p3 = new Player("3", "3", 100, false, false, null);

        GameState gameState = new GameState(new Deck(random), List.of(p1, p2, p3));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        TestPlayerState.setCurrentPlayer(gameState, p2);
        gameState.getPotState().addAnte(p1, 40);
        gameState.getPotState().addAnte(p3, 40);

        new AllIn("2").apply(gameState, configuration);

        assertTrue(p1.isReady());
    }

    @Test
    void shortAllInThatIncreasesMaxContributionWithoutFullRaiseDoesNotResetReadyPlayers() throws ActionValidationException {
        Player p1 = new Player("1", "1", 200, true, false, null);
        Player p2 = new Player("2", "2", 120, false, false, null);
        Player p3 = new Player("3", "3", 200, false, false, null);

        GameState gameState = new GameState(new Deck(random), List.of(p1, p2, p3));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        TestPlayerState.setCurrentPlayer(gameState, p2);

        gameState.getPotState().addBet(p1, 100);
        gameState.getPotState().addAnte(p2, 90);

        new AllIn("2").apply(gameState, configuration);

        assertTrue(p1.isReady());
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

package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.action.ActionType;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import com.molszewski.demos.poker.test.TestPlayerState;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class ValidActionsResponseTest {

    @Test
    void returnsReadyActionWithTargetReadyStateInNotStartedPhase() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));
        new Join("1", "Alice").apply(state, configuration);

        ValidActionsResponse response = ValidActionsResponse.fromGameState("1", state, configuration);
        Map<ActionType, ValidActionsResponse.ValidAction> byType = toMap(response);

        assertEquals(1, response.validActions().size());
        assertTrue(byType.containsKey(ActionType.READY));
        assertEquals("true", byType.get(ActionType.READY).properties().get("ready"));
    }

    @Test
    void returnsBetRangeWhenNoBetPlacedInBettingPhase() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));
        new Join("1", "Alice").apply(state, configuration);
        new Join("2", "Bob").apply(state, configuration);

        Player me = state.getPlayerState().getById("1");
        me.setFolded(false);
        me.setReady(false);
        state.getPlayerState().getById("2").setFolded(false);
        state.setGamePhase(GamePhase.FIRST_BETTING);
        TestPlayerState.setCurrentPlayer(state, me);
        state.getPotState().addAnte(me, configuration.ante());
        state.getPotState().addAnte(state.getPlayerState().getById("2"), configuration.ante());

        ValidActionsResponse response = ValidActionsResponse.fromGameState("1", state, configuration);
        Map<ActionType, ValidActionsResponse.ValidAction> byType = toMap(response);

        assertTrue(byType.containsKey(ActionType.CHECK));
        assertTrue(byType.containsKey(ActionType.BET));
        assertTrue(byType.containsKey(ActionType.ALL_IN));
        assertTrue(byType.containsKey(ActionType.FOLD));
        assertFalse(byType.containsKey(ActionType.CALL));
        assertFalse(byType.containsKey(ActionType.RAISE));
        assertEquals("15", byType.get(ActionType.BET).properties().get("min"));
        assertEquals("300", byType.get(ActionType.BET).properties().get("max"));
    }

    @Test
    void returnsCallAndRaiseRangeWhenPlayerIsBehindCurrentBet() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));
        new Join("1", "Alice").apply(state, configuration);
        new Join("2", "Bob").apply(state, configuration);

        Player me = state.getPlayerState().getById("1");
        Player other = state.getPlayerState().getById("2");
        me.setFolded(false);
        me.setReady(false);
        other.setFolded(false);
        state.setGamePhase(GamePhase.FIRST_BETTING);
        TestPlayerState.setCurrentPlayer(state, me);
        state.getPotState().addAnte(me, configuration.ante());
        state.getPotState().addAnte(other, configuration.ante());
        state.getPotState().addBet(other, 25);

        ValidActionsResponse response = ValidActionsResponse.fromGameState("1", state, configuration);
        Map<ActionType, ValidActionsResponse.ValidAction> byType = toMap(response);

        assertTrue(byType.containsKey(ActionType.CALL));
        assertTrue(byType.containsKey(ActionType.RAISE));
        assertTrue(byType.containsKey(ActionType.ALL_IN));
        assertTrue(byType.containsKey(ActionType.FOLD));
        assertFalse(byType.containsKey(ActionType.CHECK));
        assertFalse(byType.containsKey(ActionType.BET));
        assertEquals("50", byType.get(ActionType.RAISE).properties().get("min"));
        assertEquals("300", byType.get(ActionType.RAISE).properties().get("max"));
    }

    @Test
    void throwsWhenPlayerIsNotInGame() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));
        new Join("1", "Alice").apply(state, configuration);

        assertThrows(IllegalStateException.class, () -> ValidActionsResponse.fromGameState("2", state, configuration));
    }

    private static Map<ActionType, ValidActionsResponse.ValidAction> toMap(ValidActionsResponse response) {
        return response.validActions().stream()
                .collect(Collectors.toMap(
                        ValidActionsResponse.ValidAction::type,
                        Function.identity()
                ));
    }
}

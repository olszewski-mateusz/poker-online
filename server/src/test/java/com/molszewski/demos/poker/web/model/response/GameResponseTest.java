package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Join;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.state.history.PotWinnerEntry;
import com.molszewski.demos.poker.engine.game.state.pot.PotType;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

@UnitTest
class GameResponseTest {

    @Test
    void mapsJoinAndPhaseHistoryEntries() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));

        new Join("1", "Alice").apply(state, configuration);
        state.setGamePhase(GamePhase.FIRST_BETTING);

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(state)
                        .configuration(configuration)
                        .build()
        );

        assertEquals(2, response.history().size());
        assertEquals("JOIN", response.history().get(0).entryType());
        assertEquals("PHASE_CHANGE", response.history().get(1).entryType());
    }

    @Test
    void mapsPotWinnerEntryWithCurrentShape() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));

        new Join("1", "Alice").apply(state, configuration);
        state.getHistory().getEntries().add(new PotWinnerEntry(120, PotType.MAIN, Set.of("1"), true));

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(state)
                        .configuration(configuration)
                        .build()
        );

        HistoryEntryResponse potWinner = response.history().stream()
                .filter(entry -> "POT_WINNER".equals(entry.entryType()))
                .findFirst()
                .orElseThrow();

        assertInstanceOf(HistoryEntryResponse.PotWinnerDetails.class, potWinner.details());
        HistoryEntryResponse.PotWinnerDetails details = (HistoryEntryResponse.PotWinnerDetails) potWinner.details();
        assertEquals(120, details.potAmount());
    }

    @Test
    void mapsPlayerContributionAsBetAndHidesOtherCardsOutsideShowdown() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));

        new Join("1", "Alice").apply(state, configuration);
        new Join("2", "Bob").apply(state, configuration);
        state.setGamePhase(GamePhase.FIRST_BETTING);
        state.getPotState().addAnte(state.getPlayerById("1"), 5);
        state.getPotState().addAnte(state.getPlayerById("2"), 7);

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(state)
                        .configuration(configuration)
                        .build()
        );

        PlayerResponse me = response.players().get(0);
        PlayerResponse opponent = response.players().get(1);

        assertEquals(5, me.bet());
        assertEquals(7, opponent.bet());
        assertNull(opponent.cards());
        assertNull(opponent.handType());
    }
}

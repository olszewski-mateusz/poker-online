package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameEngine;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Join;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
class GameResponseTest {

    @Test
    void mapsHistoryEntriesToResponseModelTest() throws GameStateException {
        GameConfiguration localConfig = GameConfiguration.defaultConfiguration();
        GameState localState = new GameState(new Deck(new Random(17L)));
        GameEngine localEngine = new GameEngine(localState, localConfig);

        new Join("1", "Alice").apply(localState, localConfig);
        localState.setGamePhase(GamePhase.FIRST_BETTING);

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(localEngine.getGameState())
                        .configuration(localEngine.getConfiguration())
                        .build()
        );

        HistoryEntryResponse actionEntry = response.history().getFirst();
        HistoryEntryResponse phaseChangeEntry = response.history().get(1);

        assertEquals(2, response.history().size());
        assertEquals("JOIN", actionEntry.entryType());
        assertInstanceOf(HistoryEntryResponse.ActionDetails.class, actionEntry.details());
        assertEquals("PHASE_CHANGE", phaseChangeEntry.entryType());
        assertEquals(GamePhase.FIRST_BETTING, phaseChangeEntry.details());
    }
}

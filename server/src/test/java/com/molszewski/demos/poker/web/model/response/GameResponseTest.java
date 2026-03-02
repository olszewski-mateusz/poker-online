package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.Join;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.pot.Pot;
import com.molszewski.demos.poker.engine.pot.PotType;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals("ACTION", response.history().get(0).category());
        assertEquals("join", response.history().get(0).properties().get("actionType"));
        assertEquals("PHASE_CHANGE", response.history().get(1).category());
        assertEquals(GamePhase.FIRST_BETTING.toString(), response.history().get(1).properties().get("newPhase"));
    }

    @Test
    void mapsPotWinnerEntryWithCurrentShape() throws ActionValidationException {
        GameConfiguration configuration = GameConfiguration.defaultConfiguration();
        GameState state = new GameState(new Deck(new Random(17L)));
        Hand winningHand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.TEN, Suit.CLUBS)
        ));

        new Join("1", "Alice").apply(state, configuration);
        Player player1 = state.getPlayerById("1");
        player1.setHand(winningHand);
        state.getHistory().addPotWinner(
                state,
                new Pot(PotType.MAIN, Map.of("1", 120), Set.of("1")),
                List.of(player1),
                true
        );

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(state)
                        .configuration(configuration)
                        .build()
        );

        HistoryEntryResponse potWinner = response.history().stream()
                .filter(entry -> "POT_WINNER".equals(entry.category()))
                .findFirst()
                .orElseThrow();

        assertEquals("MAIN", potWinner.properties().get("potType"));
        assertEquals("120", potWinner.properties().get("potChips"));
        assertEquals("Alice", potWinner.properties().get("winners"));
        assertEquals("true", potWinner.properties().get("showdown"));
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

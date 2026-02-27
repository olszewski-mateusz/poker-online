package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;
import com.molszewski.demos.poker.engine.card.Suit;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameEngine;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Join;
import com.molszewski.demos.poker.engine.game.state.history.PotWinnerEntry;
import com.molszewski.demos.poker.engine.game.state.history.ShowdownSummaryEntry;
import com.molszewski.demos.poker.engine.game.state.history.WinnerEntry;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.hand.HandType;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

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

    @Test
    void mapsPotWinnerEntryToResponseModelTest() throws GameStateException {
        GameConfiguration localConfig = GameConfiguration.defaultConfiguration();
        GameState localState = new GameState(new Deck(new Random(17L)));

        new Join("1", "Alice").apply(localState, localConfig);
        new Join("2", "Bob").apply(localState, localConfig);

        localState.getHistory().getEntries().add(new PotWinnerEntry(1, 101, java.util.List.of("1", "2")));

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(localState)
                        .configuration(localConfig)
                        .build()
        );

        HistoryEntryResponse potWinnerEntry = response.history().stream()
                .filter(entry -> "POT_WINNER".equals(entry.entryType()))
                .findFirst()
                .orElseThrow();

        assertInstanceOf(HistoryEntryResponse.PotWinnerDetails.class, potWinnerEntry.details());
        HistoryEntryResponse.PotWinnerDetails details = (HistoryEntryResponse.PotWinnerDetails) potWinnerEntry.details();
        assertEquals(1, details.potIndex());
        assertEquals(101, details.potAmount());
        assertEquals(java.util.List.of("Alice", "Bob"), details.winnerNames());
    }

    @Test
    void mapsWinnerEntryWithKeyCardsToResponseModelTest() throws GameStateException {
        GameConfiguration localConfig = GameConfiguration.defaultConfiguration();
        GameState localState = new GameState(new Deck(new Random(17L)));

        new Join("1", "Alice").apply(localState, localConfig);
        Hand hand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.CLUBS)
        ));
        localState.getHistory().getEntries().add(new WinnerEntry("1", HandType.PAIR, hand.getKeyCards()));

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(localState)
                        .configuration(localConfig)
                        .build()
        );

        HistoryEntryResponse winnerEntry = response.history().stream()
                .filter(entry -> "WINNER".equals(entry.entryType()))
                .findFirst()
                .orElseThrow();

        assertInstanceOf(HistoryEntryResponse.WinnerDetails.class, winnerEntry.details());
        HistoryEntryResponse.WinnerDetails details = (HistoryEntryResponse.WinnerDetails) winnerEntry.details();
        assertEquals("Alice", details.playerName());
        assertEquals(HandType.PAIR, details.handType());
        assertEquals(5, details.keyCards().size());
        assertEquals(Rank.ACE, details.keyCards().get(0).rank());
    }

    @Test
    void mapsShowdownSummaryEntryToResponseModelTest() throws GameStateException {
        GameConfiguration localConfig = GameConfiguration.defaultConfiguration();
        GameState localState = new GameState(new Deck(new Random(17L)));

        new Join("1", "Alice").apply(localState, localConfig);
        new Join("2", "Bob").apply(localState, localConfig);
        Hand hand = new Hand(Set.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.CLUBS)
        ));

        localState.getHistory().getEntries().add(new ShowdownSummaryEntry(
                java.util.List.of(new ShowdownSummaryEntry.PotSummary(1, 50, java.util.List.of("1"))),
                java.util.List.of(new ShowdownSummaryEntry.PlayerSummary("1", HandType.PAIR, hand.getKeyCards()))
        ));

        GameResponse response = GameResponse.fromParams(
                GameResponse.GameContext.builder()
                        .gameId("game")
                        .myId("1")
                        .state(localState)
                        .configuration(localConfig)
                        .build()
        );

        HistoryEntryResponse summaryEntry = response.history().stream()
                .filter(entry -> "SHOWDOWN_SUMMARY".equals(entry.entryType()))
                .findFirst()
                .orElseThrow();

        assertInstanceOf(HistoryEntryResponse.ShowdownSummaryDetails.class, summaryEntry.details());
        HistoryEntryResponse.ShowdownSummaryDetails details =
                (HistoryEntryResponse.ShowdownSummaryDetails) summaryEntry.details();
        assertEquals(1, details.pots().size());
        assertEquals(1, details.winners().size());
        assertEquals(50, details.pots().getFirst().potAmount());
        assertEquals("Alice", details.pots().getFirst().winnerNames().getFirst());
        assertEquals("Alice", details.winners().getFirst().playerName());
        assertEquals(HandType.PAIR, details.winners().getFirst().handType());
        assertEquals(5, details.winners().getFirst().keyCards().size());
    }
}

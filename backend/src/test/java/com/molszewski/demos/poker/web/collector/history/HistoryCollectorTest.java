package com.molszewski.demos.poker.web.collector.history;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.hand.Hand;
import com.molszewski.demos.poker.core.hand.HandType;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.persistence.entity.command.CheckCommand;
import com.molszewski.demos.poker.persistence.entity.command.JoinCommand;
import com.molszewski.demos.poker.persistence.entity.command.RaiseCommand;
import com.molszewski.demos.poker.web.collector.history.entry.*;
import com.molszewski.demos.poker.web.collector.metadata.MetadataCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryCollectorTest {

    private HistoryCollector historyCollector;
    private MetadataCollector metadataCollector;

    @Mock
    private Game game;

    @Mock
    private GameState gameState;

    @BeforeEach
    void setUp() {
        historyCollector = HistoryCollector.newInstance();
        metadataCollector = MetadataCollector.newInstance();
    }

    @Test
    @DisplayName("Include command")
    void includeCommandTest() {
        when(game.getPhase()).thenReturn(GamePhase.NOT_STARTED);

        JoinCommand command = new JoinCommand();
        command.setPlayerId("1");
        command.setDisplayName("test");

        metadataCollector.includeCommand(command);
        historyCollector.includeCommand(command, metadataCollector, game);
        ActionEntry entry = (ActionEntry) historyCollector.getEntries().getFirst();

        assertEquals(1, historyCollector.getEntries().size());
        assertEquals(ActionEntry.Type.JOIN.toString(), entry.getEntryType());
        assertEquals("test", entry.getDetails().playerName());
    }

    @Test
    @DisplayName("Include raise command")
    void includeRaiseCommandTest() {
        when(game.getPhase()).thenReturn(GamePhase.NOT_STARTED);

        JoinCommand joinCommand = new JoinCommand();
        joinCommand.setPlayerId("1");
        joinCommand.setDisplayName("test");

        metadataCollector.includeCommand(joinCommand);
        historyCollector.includeCommand(joinCommand, metadataCollector, game);

        RaiseCommand raiseCommand = new RaiseCommand();
        raiseCommand.setPlayerId("1");
        raiseCommand.setAmount(100);

        metadataCollector.includeCommand(raiseCommand);
        historyCollector.includeCommand(raiseCommand, metadataCollector, game);

        ActionEntry entry = (ActionEntry) historyCollector.getEntries().get(1);

        assertEquals(2, historyCollector.getEntries().size());
        assertEquals(ActionEntry.Type.RAISE.toString(), entry.getEntryType());
        assertEquals("test", entry.getDetails().playerName());
        assertEquals(100, entry.getDetails().value());
        assertTrue(metadataCollector.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Include command phase changed")
    void includeCommandPhaseChangedTest() {
        when(game.getPhase()).thenReturn(GamePhase.FIRST_BETTING);
        metadataCollector.setBetPlacedInCurrentPhase(true);

        JoinCommand joinCommand = new JoinCommand();
        joinCommand.setPlayerId("1");
        joinCommand.setDisplayName("test");

        metadataCollector.includeCommand(joinCommand);
        historyCollector.includeCommand(joinCommand, metadataCollector, game);
        ActionEntry actionEntry = (ActionEntry) historyCollector.getEntries().get(0);
        PhaseChangeEntry phaseChangeEntry = (PhaseChangeEntry) historyCollector.getEntries().get(1);

        assertEquals(2, historyCollector.getEntries().size());
        assertEquals(ActionEntry.Type.JOIN.toString(), actionEntry.getEntryType());
        assertEquals("test", actionEntry.getDetails().playerName());
        assertFalse(metadataCollector.isBetPlacedInCurrentPhase());
        assertEquals("PHASE_CHANGE", phaseChangeEntry.getEntryType());
        assertEquals(GamePhase.FIRST_BETTING, phaseChangeEntry.getDetails());
    }

    @Test
    @DisplayName("Include command phase changed showdown")
    void includeCommandPhaseChangedShowdownTest() {
        String id = "17";
        String name = "winner";
        Player winner = new Player(id, 100);
        winner.setHand(new Hand(Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.CLUBS),
                new Card(Rank.SIX, Suit.HEARTS)
        )));

        JoinCommand joinCommand = new JoinCommand();
        joinCommand.setPlayerId(id);
        joinCommand.setDisplayName(name);

        when(game.getPhase()).thenReturn(GamePhase.NOT_STARTED);

        metadataCollector.includeCommand(joinCommand);
        historyCollector.includeCommand(joinCommand, metadataCollector, game);

        when(gameState.getWinner()).thenReturn(Optional.of(winner));
        when(game.getPhase()).thenReturn(GamePhase.SHOWDOWN);
        when(game.getGameState()).thenReturn(gameState);

        CheckCommand checkCommand = new CheckCommand();
        checkCommand.setPlayerId(id);

        metadataCollector.includeCommand(checkCommand);
        historyCollector.includeCommand(checkCommand, metadataCollector, game);

        ActionEntry actionEntry = (ActionEntry) historyCollector.getEntries().get(1);
        PhaseChangeEntry phaseChangeEntry = (PhaseChangeEntry) historyCollector.getEntries().get(2);
        WinnerEntry winnerEntry = (WinnerEntry) historyCollector.getEntries().get(3);

        assertEquals(4, historyCollector.getEntries().size());
        assertEquals(ActionEntry.Type.CHECK.toString(), actionEntry.getEntryType());
        assertEquals(name, actionEntry.getDetails().playerName());
        assertFalse(metadataCollector.isBetPlacedInCurrentPhase());
        assertEquals("PHASE_CHANGE", phaseChangeEntry.getEntryType());
        assertEquals(GamePhase.SHOWDOWN, phaseChangeEntry.getDetails());
        assertEquals("WINNER", winnerEntry.getEntryType());
        assertEquals(name, winnerEntry.getDetails().playerName());
        assertEquals(HandType.STRAIGHT, winnerEntry.getDetails().handType());
    }

    @Test
    @DisplayName("Include command phase changed finished")
    void includeCommandPhaseChangedFinishedTest() {
        String id = "17";
        String name = "winner";
        Player winner = new Player(id, 100);

        JoinCommand joinCommand = new JoinCommand();
        joinCommand.setPlayerId(id);
        joinCommand.setDisplayName(name);

        when(game.getPhase()).thenReturn(GamePhase.NOT_STARTED);

        metadataCollector.includeCommand(joinCommand);
        historyCollector.includeCommand(joinCommand, metadataCollector, game);

        when(gameState.getWinner()).thenReturn(Optional.of(winner));
        when(game.getPhase()).thenReturn(GamePhase.FINISHED);
        when(game.getGameState()).thenReturn(gameState);

        CheckCommand checkCommand = new CheckCommand();
        checkCommand.setPlayerId(id);

        metadataCollector.includeCommand(checkCommand);
        historyCollector.includeCommand(checkCommand, metadataCollector, game);

        ActionEntry actionEntry = (ActionEntry) historyCollector.getEntries().get(1);
        PhaseChangeEntry phaseChangeEntry = (PhaseChangeEntry) historyCollector.getEntries().get(2);
        GameWinnerEntry winnerEntry = (GameWinnerEntry) historyCollector.getEntries().get(3);

        assertEquals(4, historyCollector.getEntries().size());
        assertEquals(ActionEntry.Type.CHECK.toString(), actionEntry.getEntryType());
        assertEquals(name, actionEntry.getDetails().playerName());
        assertFalse(metadataCollector.isBetPlacedInCurrentPhase());
        assertEquals("PHASE_CHANGE", phaseChangeEntry.getEntryType());
        assertEquals(GamePhase.FINISHED, phaseChangeEntry.getDetails());
        assertEquals("GAME_WINNER", winnerEntry.getEntryType());
        assertEquals(name, winnerEntry.getDetails().playerName());
    }
}
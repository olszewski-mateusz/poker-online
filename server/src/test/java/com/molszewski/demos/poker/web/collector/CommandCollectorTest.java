package com.molszewski.demos.poker.web.collector;

import com.molszewski.demos.poker.engine.game.GameEngine;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.action.Join;
import com.molszewski.demos.poker.engine.game.state.history.entry.ActionEntry;
import com.molszewski.demos.poker.engine.game.state.history.entry.HistoryEntry;
import com.molszewski.demos.poker.engine.game.state.history.entry.PhaseChangeEntry;
import com.molszewski.demos.poker.persistence.entity.command.JoinCommand;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
class CommandCollectorTest {

    @Test
    @DisplayName("Process command updates metadata")
    void processCommandTest() {
        CommandCollector commandCollector = CommandCollector.newInstance();
        JoinCommand command = new JoinCommand();
        command.setPlayerId("1");
        command.setDisplayName("player");

        commandCollector.processCommand(command);

        assertEquals("player", commandCollector.getMetadataCollector().getPlayerMetadata("1").name());
    }

    @Test
    @DisplayName("Get history maps engine history to response entries")
    void getHistoryTest() {
        CommandCollector commandCollector = CommandCollector.newInstance();

        JoinCommand joinCommand = new JoinCommand();
        joinCommand.setPlayerId("1");
        joinCommand.setDisplayName("player");
        commandCollector.processCommand(joinCommand);

        GameState gameState = new GameState(null);
        gameState.getHistory().addAction(new Join("1"));
        gameState.setGamePhase(GamePhase.FIRST_BETTING);
        GameEngine gameEngine = new GameEngine(gameState, null);

        List<HistoryEntry> history = commandCollector.getHistory(gameEngine);

        ActionEntry actionEntry = (ActionEntry) history.getFirst();
        PhaseChangeEntry phaseChangeEntry = (PhaseChangeEntry) history.get(1);

        assertEquals(2, history.size());
        assertEquals(ActionEntry.Type.JOIN.toString(), actionEntry.getEntryType());
        assertEquals("player", actionEntry.getDetails().playerName());
        assertEquals("PHASE_CHANGE", phaseChangeEntry.getEntryType());
        assertEquals(GamePhase.FIRST_BETTING, phaseChangeEntry.getDetails());
    }
}

package com.molszewski.demos.poker.web.collector;

import com.molszewski.demos.poker.engine.game.GameEngine;
import com.molszewski.demos.poker.engine.game.state.history.entry.ActionEntry;
import com.molszewski.demos.poker.engine.game.state.history.entry.GameWinnerEntry;
import com.molszewski.demos.poker.engine.game.state.history.entry.HistoryEntry;
import com.molszewski.demos.poker.engine.game.state.history.entry.PhaseChangeEntry;
import com.molszewski.demos.poker.engine.game.state.history.entry.WinnerEntry;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.web.collector.metadata.MetadataCollector;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class CommandCollector {
    private final MetadataCollector metadataCollector;

    private CommandCollector(MetadataCollector metadataCollector) {
        this.metadataCollector = metadataCollector;
    }

    public static CommandCollector newInstance() {
        return new CommandCollector(MetadataCollector.newInstance());
    }

    public void processCommand(Command command) {
        this.metadataCollector.includeCommand(command);
    }

    public List<HistoryEntry> getHistory(GameEngine gameEngine) {
        return gameEngine.getGameState().getHistory().getSortedEntries().stream()
                .map(this::mapEntry)
                .filter(Objects::nonNull)
                .toList();
    }

    private HistoryEntry mapEntry(com.molszewski.demos.poker.engine.game.state.history.HistoryEntry entry) {
        if (entry instanceof com.molszewski.demos.poker.engine.game.state.history.ActionEntry actionEntry) {
            String playerName = metadataCollector.getPlayerMetadata(actionEntry.getAction().getPlayerId()).name();
            return ActionEntry.fromAction(actionEntry.getAction(), playerName);
        }
        if (entry instanceof com.molszewski.demos.poker.engine.game.state.history.PhaseChangeEntry phaseChangeEntry) {
            return PhaseChangeEntry.fromPhase(phaseChangeEntry.getPhase());
        }
        if (entry instanceof com.molszewski.demos.poker.engine.game.state.history.WinnerEntry winnerEntry) {
            String playerName = metadataCollector.getPlayerMetadata(winnerEntry.getPlayerId()).name();
            return WinnerEntry.create(playerName, winnerEntry.getHandType());
        }
        if (entry instanceof com.molszewski.demos.poker.engine.game.state.history.GameWinnerEntry winnerEntry) {
            String playerName = metadataCollector.getPlayerMetadata(winnerEntry.getPlayerId()).name();
            return GameWinnerEntry.create(playerName);
        }
        return null;
    }
}

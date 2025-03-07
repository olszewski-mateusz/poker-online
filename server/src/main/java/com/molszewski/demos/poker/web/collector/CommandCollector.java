package com.molszewski.demos.poker.web.collector;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.web.collector.history.HistoryCollector;
import com.molszewski.demos.poker.web.collector.history.entry.HistoryEntry;
import com.molszewski.demos.poker.web.collector.metadata.MetadataCollector;
import lombok.Getter;

import java.util.List;

@Getter
public class CommandCollector {
    private final MetadataCollector metadataCollector;
    private final HistoryCollector historyCollector;

    private CommandCollector(MetadataCollector metadataCollector, HistoryCollector historyCollector) {
        this.metadataCollector = metadataCollector;
        this.historyCollector = historyCollector;
    }

    public static CommandCollector newInstance() {
        return new CommandCollector(MetadataCollector.newInstance(), HistoryCollector.newInstance());
    }

    public void processCommand(Command command, Game game) {
        this.metadataCollector.includeCommand(command);
        this.historyCollector.includeCommand(command, this.metadataCollector, game);
    }

    public List<HistoryEntry> getHistory() {
        return this.historyCollector.getEntries();
    }
}

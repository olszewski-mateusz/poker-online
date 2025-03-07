package com.molszewski.demos.poker.web.collector;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.JoinCommand;
import com.molszewski.demos.poker.test.UnitTest;
import com.molszewski.demos.poker.web.collector.history.HistoryCollector;
import com.molszewski.demos.poker.web.collector.metadata.MetadataCollector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@UnitTest
class CommandCollectorTest {

    @Mock
    private MetadataCollector metadataCollector;

    @Mock
    private HistoryCollector historyCollector;

    @InjectMocks
    private CommandCollector commandCollector;

    @Test
    @DisplayName("Process command")
    void processCommandTest() {
        Game game = new Game(null, null, null);
        Command command = new JoinCommand();
        commandCollector.processCommand(command, game);

        verify(metadataCollector, times(1)).includeCommand(command);
        verify(historyCollector, times(1)).includeCommand(command, metadataCollector, game);
    }

    @Test
    @DisplayName("Get history")
    void getHistoryTest() {
        commandCollector.getHistory();

        verify(historyCollector, times(1)).getEntries();
    }
}
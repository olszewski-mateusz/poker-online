package com.molszewski.demos.poker.web.collector.metadata;

import com.molszewski.demos.poker.persistence.entity.command.CheckCommand;
import com.molszewski.demos.poker.persistence.entity.command.JoinCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetadataCollectorTest {

    private MetadataCollector collector;

    @BeforeEach
    void setUp() {
        collector = MetadataCollector.newInstance();
    }

    @Test
    @DisplayName("Include join command")
    void includeJoinCommandTest() {
        String name = "test";
        String id = "1";
        JoinCommand command = new JoinCommand();
        command.setPlayerId(id);
        command.setDisplayName(name);

        collector.includeCommand(command);

        PlayerMetadata metadata = collector.getPlayerMetadata(id);

        assertEquals(id, metadata.id());
        assertEquals(name, metadata.name());
        assertEquals(1, metadata.index());
        assertFalse(collector.isBetPlacedInCurrentPhase());
    }

    @Test
    @DisplayName("Include other command")
    void includeOtherCommandTest() {
        String id = "1";
        CheckCommand command = new CheckCommand();
        command.setPlayerId(id);

        collector.includeCommand(command);

        assertThrows(IllegalArgumentException.class, () -> collector.getPlayerMetadata(id));
    }

    @Test
    @DisplayName("Include multiple join commands")
    void getPlayerMetadataTest() {
        JoinCommand command1 = new JoinCommand();
        command1.setPlayerId("1");
        command1.setDisplayName("test1");

        JoinCommand command2 = new JoinCommand();
        command2.setPlayerId("2");
        command2.setDisplayName("test2");

        collector.includeCommand(command1);
        collector.includeCommand(command2);

        PlayerMetadata metadata1 = collector.getPlayerMetadata("1");
        PlayerMetadata metadata2 = collector.getPlayerMetadata("2");

        assertEquals("1", metadata1.id());
        assertEquals("test1", metadata1.name());
        assertEquals(1, metadata1.index());
        assertEquals("2", metadata2.id());
        assertEquals("test2", metadata2.name());
        assertEquals(2, metadata2.index());
    }

    @Test
    @DisplayName("Get non existing player metadata throws")
    void getPlayerMetadataThrowsTest() {
        assertThrows(IllegalArgumentException.class, () -> collector.getPlayerMetadata("1"));
    }

    @Test
    @DisplayName("Get non existing player index")
    void getPlayerIndexThrowsTest() {
        assertThrows(IllegalArgumentException.class, () -> collector.getPlayerIndex("1"));
    }
}
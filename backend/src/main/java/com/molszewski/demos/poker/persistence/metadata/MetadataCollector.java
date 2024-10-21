package com.molszewski.demos.poker.persistence.metadata;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.JoinCommand;

import java.util.ArrayList;
import java.util.List;

public class MetadataCollector {

    private MetadataCollector() {
    }

    private final List<PlayerMetadata> playersMetadata = new ArrayList<>();

    public void includeCommand(Command command) {
        if (command instanceof JoinCommand joinCommand) {
            playersMetadata.add(new PlayerMetadata(joinCommand.getPlayerId(), joinCommand.getDisplayName()));
        }
    }

    public PlayerMetadata getPlayerMetadata(String playerId) {
        return playersMetadata.stream().filter(playerMetadata -> playerMetadata.id().equals(playerId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public static MetadataCollector newInstance() {
        return new MetadataCollector();
    }
}

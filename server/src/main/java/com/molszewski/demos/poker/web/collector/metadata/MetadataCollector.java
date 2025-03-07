package com.molszewski.demos.poker.web.collector.metadata;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.entity.command.JoinCommand;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class MetadataCollector {

    private MetadataCollector() {
    }

    private final List<PlayerMetadata> playersMetadata = new ArrayList<>();

    @Getter @Setter
    private boolean betPlacedInCurrentPhase = false;

    public void includeCommand(Command command) {
        if (command instanceof JoinCommand joinCommand) {
            int index = playersMetadata.size() + 1;
            playersMetadata.add(new PlayerMetadata(joinCommand.getPlayerId(), index, joinCommand.getDisplayName()));
        }
    }

    public PlayerMetadata getPlayerMetadata(String playerId) {
        return playersMetadata.stream().filter(playerMetadata -> playerMetadata.id().equals(playerId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public Integer getPlayerIndex(String playerId) {
        return playersMetadata.stream().filter(playerMetadata -> playerMetadata.id().equals(playerId)).findFirst()
                .map(PlayerMetadata::index)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public static MetadataCollector newInstance() {
        return new MetadataCollector();
    }
}

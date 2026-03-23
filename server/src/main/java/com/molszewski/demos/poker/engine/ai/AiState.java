package com.molszewski.demos.poker.engine.ai;

import com.molszewski.demos.poker.engine.player.Player;

import java.util.HashMap;
import java.util.Map;

public class AiState {

    private final Map<String, AiConfiguration> configurations = new HashMap<>();

    public void addAi(String playerId, AiConfiguration config) {
        if (configurations.containsKey(playerId)) {
            throw new IllegalArgumentException(String.format("Ai configuration for player %s already exists",  playerId));
        }
        configurations.put(playerId, config);
    }

    public void addAi(Player player, AiConfiguration config) {
        addAi(player.getId(), config);
    }

    public void removeAi(Player player) {
        removeAi(player.getId());
    }

    public void removeAi(String playerId) {
        if (!configurations.containsKey(playerId)) {
            throw new IllegalArgumentException(String.format("Ai configuration for player %s doesn't exist",  playerId));
        }
        configurations.remove(playerId);
    }

    public AiConfiguration getAi(Player player) {
        return getAi(player.getId());
    }

    public AiConfiguration getAi(String playerId) {
        if (!configurations.containsKey(playerId)) {
            throw new IllegalArgumentException(String.format("Ai configuration for player %s doesn't exist", playerId));
        }
        return configurations.get(playerId);
    }

    public boolean isPlayerAi(String playerId) {
        return configurations.containsKey(playerId);
    }
}

package com.molszewski.demos.poker.engine.game.state.pot;

import java.util.Map;
import java.util.Set;

public record Pot(
        PotType type,
        Map<String, Integer> contributions,
        Set<String> eligible) {

    public Set<String> getContributors() {
        return contributions.keySet();
    }

    public int getContribution(String playerId) {
        return contributions.getOrDefault(playerId, 0);
    }

    public int getAmount() {
        return contributions.values().stream().mapToInt(Integer::intValue).sum();
    }
}

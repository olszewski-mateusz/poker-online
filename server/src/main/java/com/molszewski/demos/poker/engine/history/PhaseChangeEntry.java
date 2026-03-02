package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import lombok.Getter;

import java.util.Map;

@Getter
class PhaseChangeEntry extends HistoryEntry<GamePhase> {

    PhaseChangeEntry(GameState gameState, GamePhase phase) {
        super(gameState, phase);
    }

    @Override
    public String getEntryCategory() {
        return "PHASE_CHANGE";
    }

    @Override
    protected Map<String, String> buildProperties(GameState gameState, GamePhase phase) {
        return Map.of("newPhase", phase.toString());
    }
}

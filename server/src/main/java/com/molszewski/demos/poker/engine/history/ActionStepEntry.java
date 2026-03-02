package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.game.GameState;
import lombok.Getter;

import java.util.Map;

@Getter
class ActionStepEntry extends HistoryEntry<ActionStepType> {

    ActionStepEntry(GameState gameState, ActionStepType step) {
        super(gameState, step);
    }

    @Override
    public String getEntryCategory() {
        return "ACTION_STEP";
    }

    @Override
    protected Map<String, String> buildProperties(GameState gameState, ActionStepType actionStep) {
        return Map.of("stepType", actionStep.toString());
    }

}

package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;

public interface Transition {
    void apply(GameState gameState, GameConfiguration configuration);
}

package com.molszewski.demos.poker.engine.action.step;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;

public class ActionSteps {

    public static void nextTurn (GameState state) {
        NextTurn.nextTurn(state);
    }

    public static void tryStartRound (GameState state, GameConfiguration configuration) {
        StartRound.tryStartRound(state, configuration);
    }
}

package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;

public abstract sealed class Transition permits StartGame, NextTurn {

    protected Transition() {
    }

    public static void startGame(StateManager stateManager, Board board, GameConfiguration configuration) {
        Transition transition = new StartGame();
        transition.applyLogic(stateManager, board, configuration);
    }

    public static void nextTurn(StateManager stateManager, Board board, GameConfiguration configuration) {
        Transition transition = new NextTurn();
        transition.applyLogic(stateManager, board, configuration);
    }

    protected abstract void applyLogic(StateManager stateManager, Board board, GameConfiguration configuration);
}

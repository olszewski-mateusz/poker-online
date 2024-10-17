package com.molszewski.demos.poker.core.game.state;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.action.ActionException;
import com.molszewski.demos.poker.core.game.action.Action;
import com.molszewski.demos.poker.core.player.Player;

public interface StateManager {
    GameState getState();

    void setState(GameState newState);

    Player getCurrentPlayer();

    void setCurrentPlayer(Player newCurrentPlayer);

    void executeAction(Action action, Board board, GameConfiguration configuration) throws ActionException;
}

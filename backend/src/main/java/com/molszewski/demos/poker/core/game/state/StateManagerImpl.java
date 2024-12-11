package com.molszewski.demos.poker.core.game.state;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.action.*;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.transition.Transition;
import com.molszewski.demos.poker.core.player.Player;

import java.util.Set;

public class StateManagerImpl implements StateManager {

    private Player currentPlayer;
    private GameState gameState = GameState.NOT_STARTED;

    @Override
    public GameState getState() {
        return gameState;
    }

    @Override
    public void setState(GameState newState) {
        this.gameState = newState;
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public void setCurrentPlayer(Player newCurrentPlayer) {
        this.currentPlayer = newCurrentPlayer;
    }

    @Override
    public void executeAction(Action action, Board board, GameConfiguration configuration) throws ActionException {
        switch (action) {
            case Join joinAction -> {
                this.checkIfLegalState(Set.of(GameState.NOT_STARTED));
                joinAction.execute(board, configuration);
            }
            case Ready readyAction -> {
                this.checkIfLegalState(Set.of(GameState.NOT_STARTED, GameState.SHOWDOWN));

                readyAction.execute(board, configuration);
                Transition.startGame(this, board, configuration);
            }
            case Check checkAction -> {
                this.checkIfLegalState(Set.of(GameState.FIRST_BETTING, GameState.SECOND_BETTING));
                this.checkIfPlayersTurn(checkAction.getPlayerId());

                checkAction.execute(board, configuration);

                Transition.nextTurn(this, board, configuration);
            }
            case Raise raiseAction -> {
                this.checkIfLegalState(Set.of(GameState.FIRST_BETTING, GameState.SECOND_BETTING));
                this.checkIfPlayersTurn(raiseAction.getPlayerId());

                raiseAction.execute(board, configuration);

                Transition.nextTurn(this, board, configuration);
            }
            case AllIn allInAction -> {
                this.checkIfLegalState(Set.of(GameState.FIRST_BETTING, GameState.SECOND_BETTING));
                this.checkIfPlayersTurn(allInAction.getPlayerId());

                allInAction.execute(board, configuration);

                Transition.nextTurn(this, board, configuration);
            }
            case Fold foldAction -> {
                this.checkIfLegalState(Set.of(GameState.FIRST_BETTING, GameState.SECOND_BETTING));
                this.checkIfPlayersTurn(foldAction.getPlayerId());

                foldAction.execute(board, configuration);

                Transition.nextTurn(this, board, configuration);
            }
            case Replace replaceAction -> {
                this.checkIfLegalState(Set.of(GameState.DRAWING));
                this.checkIfPlayersTurn(replaceAction.getPlayerId());

                replaceAction.execute(board, configuration);

                Transition.nextTurn(this, board, configuration);
            }
        }
    }

    private void checkIfPlayersTurn(String playerId) throws ActionException {
        if (currentPlayer.getId().equals(playerId)) {
            if (currentPlayer.isReady()) {
                throw new ActionException(String.format("Player %s is ready", currentPlayer.getId()));
            }
            if (currentPlayer.isFolded()) {
                throw new ActionException(String.format("Player %s folded", currentPlayer.getId()));
            }
        } else {
            throw new ActionException("It's not player " + playerId + "turn");
        }
    }

    private void checkIfLegalState(Set<GameState> legalStates) throws ActionException {
        if (!legalStates.contains(gameState)) {
            throw new ActionException("Action not legal in state " + gameState);
        }
    }
}

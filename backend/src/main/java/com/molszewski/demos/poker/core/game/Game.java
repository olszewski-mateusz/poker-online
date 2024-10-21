package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.game.state.GameState;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.player.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Game {
    @Getter
    private final Board board;
    private final StateManager stateManager;
    @Getter
    private final GameConfiguration configuration;

    public Game(Board board, StateManager stateManager, GameConfiguration configuration) {
        this.board = board;
        this.stateManager = stateManager;
        this.configuration = configuration;
    }

    public Player getCurrentPlayer() {
        return stateManager.getCurrentPlayer();
    }

    public boolean applyAction(Action action) {
        try {
            stateManager.executeAction(action, board, configuration);
            return true;
        } catch (ActionException e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    public GameState getGameState() {
        return this.stateManager.getState();
    }

    public List<Player> getPlayers() {
        return this.board.getPlayers().stream().map(Player::copy).toList();
    }

    public int getCardsInDeck() {
        return this.board.getDeck().cardsCount();
    }

    public int getDiscardedCards() {
        return this.board.getDeck().discardsCount();
    }
}

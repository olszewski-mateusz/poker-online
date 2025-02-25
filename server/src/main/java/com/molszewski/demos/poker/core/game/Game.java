package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.game.state.GamePhase;
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
    private final GameState gameState;
    private final StateManager stateManager;
    @Getter
    private final GameConfiguration configuration;

    public Game(GameState gameState, StateManager stateManager, GameConfiguration configuration) {
        this.gameState = gameState;
        this.stateManager = stateManager;
        this.configuration = configuration;
    }

    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }

    public boolean applyAction(Action action) {
        try {
            stateManager.executeAction(action, gameState, configuration);
            return true;
        } catch (ActionException e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    public GamePhase getPhase() {
        return this.gameState.getGamePhase();
    }

    public List<Player> getPlayers() {
        return this.gameState.getPlayers().stream().map(Player::copy).toList();
    }

    public int getCardsInDeck() {
        return this.gameState.getDeck().cardsCount();
    }

    public int getDiscardedCards() {
        return this.gameState.getDeck().discardsCount();
    }
}

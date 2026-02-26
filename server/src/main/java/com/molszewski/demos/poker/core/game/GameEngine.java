package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.exception.GameStateException;
import com.molszewski.demos.poker.core.player.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GameEngine {
    @Getter
    private final GameState gameState;
    @Getter
    private final GameConfiguration configuration;

    public GameEngine(GameState gameState, GameConfiguration configuration) {
        this.gameState = gameState;
        this.configuration = configuration;
    }

    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }

    public boolean applyAction(Action action) {
        try {
            action.apply(gameState, configuration);
            return true;
        } catch (GameStateException e) {
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

package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

    public boolean applyAction(Action action) {
        try {
            action.apply(gameState, configuration);
            return true;
        } catch (GameStateException e) {
            log.warn(e.getMessage());
            return false;
        }
    }
}

package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Stream;

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

    public void applyActions(Stream<Action> actions) {
        actions.forEach(action -> {
            try {
                action.apply(gameState, configuration);
            } catch (ActionValidationException e) {
                log.debug("Illegal action found, reason: {}", e.getMessage());
            }
        });
    }

    public void tryApplyAction(Action action) throws ActionValidationException {
        action.apply(gameState, configuration);
    }
}

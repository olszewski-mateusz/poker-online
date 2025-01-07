package com.molszewski.demos.poker.persistence.entity;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import lombok.Builder;

import java.util.Random;

@Builder
public record GameSetup(
        String id,
        long seed,
        GameConfiguration configuration
) {
    public static GameSetup init(String gameId, Random random) {
        return GameSetup.builder()
                .id(gameId)
                .configuration(GameConfiguration.defaultConfiguration())
                .seed(random.nextLong())
                .build();
    }

    public Game toGame() {
        Random random = new Random(seed);
        GameState gameState = new GameState(new Deck(random));
        StateManager stateManager = new StateManagerImpl();
        return new Game(gameState, stateManager, configuration);
    }
}

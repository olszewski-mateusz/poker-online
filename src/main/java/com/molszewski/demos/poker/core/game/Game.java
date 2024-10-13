package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.action.Action;
import com.molszewski.demos.poker.core.deck.Deck;

import java.util.Random;

public class Game {
    private final GameContext context;

    public Game() {
        this.context = new GameContext(new Deck(new Random()), GameConfiguration.defaultConfiguration());
    }

    public void applyAction(Action action) {
        this.context.applyAction(action);
    }

    public GameConfiguration getConfiguration() {
        return this.context.getConfiguration();
    }

    public GameState getGameState() {
        return this.context.getGameState();
    }
}

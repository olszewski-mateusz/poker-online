package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.action.Action;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.player.Player;

import java.util.List;
import java.util.Random;

public class Game {
    private final GameContext context;

    public Game() {
        this.context = new GameContext(new Deck(new Random()), GameConfiguration.defaultConfiguration());
    }

    public Game(Random random) {
        this.context = new GameContext(new Deck(random), GameConfiguration.defaultConfiguration());
    }

    public Game(Random random, GameConfiguration gameConfiguration) {
        this.context = new GameContext(new Deck(random), gameConfiguration);
    }

    public Game(Deck deck, GameConfiguration gameConfiguration) {
        this.context = new GameContext(deck, gameConfiguration);
    }

    public Player getCurrentPlayer() {
        return context.getCurrentPlayer();
    }

    public boolean applyAction(Action action) {
        try {
            action.execute(this.context);
            return true;
        } catch (GameException e) {
            return false;
        }
    }

    public GameConfiguration getConfiguration() {
        return this.context.getConfiguration();
    }

    public GameState getGameState() {
        return this.context.getGameState();
    }

    public List<Player> getPlayers() {
        return List.copyOf(this.context.getPlayers()); // todo: add copy of players
    }
}

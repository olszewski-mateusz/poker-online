package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GameContext {
    private final Deck deck;
    private final GameConfiguration configuration;
    private final List<Player> players = new ArrayList<>();
    private Player currentPlayer;
    private GameState gameState = GameState.NOT_STARTED;

    GameContext(Deck deck, GameConfiguration configuration) {
        this.deck = deck;
        this.configuration = configuration;
    }

    public Player getPlayerById(UUID id) throws GameException {
        return players.stream().filter(player -> player.getId().equals(id))
                .findFirst().orElseThrow(() -> new GameException("Player not found"));
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(UUID playerId) throws GameException {
        if (players.stream().anyMatch(player -> player.getId().equals(playerId))) {
            throw new GameException("Player already in game");
        }
        players.add(new Player(playerId, configuration.startMoney()));
    }

    public void changeState(GameState newState) {
        gameState = newState;
    }

    public GameConfiguration getConfiguration() {
        return configuration;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Deck getDeck() {
        return deck;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void nextCurrentPlayer() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }
        if (currentPlayer == null) {
            currentPlayer = players.getFirst();
        } else {
            currentPlayer = players.get((players.indexOf(currentPlayer) + 1) % players.size());
        }
    }

}

package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.action.Action;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private Player currentPlayer;
    private List<Player> players = new ArrayList<>();
    private GameState gameState = GameState.NOT_STARTED;
    private final Deck deck;

    public Game(Deck deck) {
        this.deck = deck;
    }

    public void applyAction(Action action) {
        action.execute(this);
    }

    public Player getPlayerById(UUID id) {
        return players.stream().filter(player -> player.getId().equals(id))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public void addPlayer(UUID playerId) {
        players.add(new Player(playerId));
    }

    public int playerCount() {
        return players.size();
    }

    public void changeState(GameState newState) {
        gameState = newState;
    }
}

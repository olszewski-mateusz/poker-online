package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.player.Player;
import lombok.Getter;

import java.util.*;

@Getter
public class Board {
    private final Deck deck;
    private final List<Player> players;

    public Board(Deck deck) {
        this.deck = deck;
        this.players = new ArrayList<>();
    }

    public Board(Deck deck, List<Player> players) {
        this.deck = deck;
        this.players = new ArrayList<>(players);
    }

    public Optional<Player> getPlayerById(String id) {
        return players.stream().filter(player -> player.getId().equals(id)).findFirst();
    }

    public int getCurrentBet() {
        return players.stream().map(Player::getBet).max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("No player in game"));
    }

    public Player getWinner() {
        return players.stream().max(Comparator.comparing(Player::getHand))
                .orElseThrow(() -> new IllegalStateException("No player in game"));
    }

    public void addPlayer(String playerId, int startMoney) {
        if (players.stream().anyMatch(player -> player.getId().equals(playerId))) {
            throw new IllegalStateException("Player already in game");
        }
        players.add(new Player(playerId, startMoney));
    }
}

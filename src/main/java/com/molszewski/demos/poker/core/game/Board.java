package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.player.Player;
import lombok.Getter;

import java.util.*;

@Getter
public class Board {
    private final Deck deck;
    private final List<Player> players = new ArrayList<>();

    Board(Deck deck) {
        this.deck = deck;
    }

    public Optional<Player> getPlayerById(UUID id) {
        return players.stream().filter(player -> player.getId().equals(id)).findFirst();
    }

    public int getCurrentBid() {
        return players.stream().map(Player::getBid).max(Comparator.naturalOrder()).orElse(0);
    }

    public Player addPlayer(UUID playerId, int startMoney) {
        if (players.stream().anyMatch(player -> player.getId().equals(playerId))) {
            throw new IllegalStateException("Player already in game");
        }
        Player newPlayer = new Player(playerId, startMoney);
        players.add(newPlayer);
        return newPlayer;
    }
}

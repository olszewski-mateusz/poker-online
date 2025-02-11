package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class GameState {
    private final Deck deck;
    private final List<Player> players;
    @Setter
    private Player currentPlayer;
    @Setter
    private GamePhase gamePhase = GamePhase.NOT_STARTED;

    public GameState(Deck deck) {
        this.deck = deck;
        this.players = new ArrayList<>();
    }

    public GameState(Deck deck, List<Player> players) {
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

    public Optional<Player> getWinner() {
        if (gamePhase == GamePhase.SHOWDOWN) {
            return players.stream()
                    .filter(p -> !p.isFolded() && p.getHand() != null)
                    .max(Comparator.comparing(Player::getHand));
        } else if (gamePhase == GamePhase.FINISHED) {
            return players.stream().filter(p -> !p.isFolded()).findFirst();
        }
        return Optional.empty();
    }

    public void addPlayer(String playerId, int startMoney) {
        if (players.stream().anyMatch(player -> player.getId().equals(playerId))) {
            throw new IllegalStateException("Player already in game");
        }
        players.add(new Player(playerId, startMoney));
    }
}

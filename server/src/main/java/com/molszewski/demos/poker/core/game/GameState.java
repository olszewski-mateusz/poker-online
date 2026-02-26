package com.molszewski.demos.poker.core.game;

import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.exception.GameStateException;
import com.molszewski.demos.poker.core.game.state.history.History;
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

    private final History history;

    public GameState(Deck deck) {
        this.deck = deck;
        this.players = new ArrayList<>();
        this.history = new History();
    }

    public GameState(Deck deck, List<Player> players) {
        this.deck = deck;
        this.players = new ArrayList<>(players);
        this.history = new History();
    }

    public Player getPlayerById(String id) {
        return players.stream().filter(player -> player.getId().equals(id)).findFirst()
                .orElseThrow();
    }

    public boolean playerExists(String id) {
        return players.stream().anyMatch(player -> player.getId().equals(id));
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
        if (playerExists(playerId)) {
            throw new IllegalStateException("Player already in game");
        }
        players.add(new Player(playerId, startMoney));
    }

    public void validatePhase(Set<GamePhase> correctPhases) throws GameStateException {
        if (!correctPhases.contains(getGamePhase())) {
            throw new GameStateException("Action not legal in phase " + getGamePhase());
        }
    }

    public void validateCurrentPlayer(String playerId) throws GameStateException {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer.getId().equals(playerId)) {
            if (currentPlayer.isReady()) {
                throw new GameStateException(String.format("Player %s is ready", currentPlayer.getId()));
            }
            if (currentPlayer.isFolded()) {
                throw new GameStateException(String.format("Player %s folded", currentPlayer.getId()));
            }
        } else {
            throw new GameStateException("It's not player " + playerId + "turn");
        }
    }
}

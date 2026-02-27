package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.game.state.history.History;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class GameState {
    private final Deck deck;
    private final List<Player> players;
    @Setter
    private Player currentPlayer;
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

    public boolean isBetPlacedInCurrentPhase() {
        int highestCommittedBet = players.stream()
                .filter(player -> !player.isFolded())
                .mapToInt(Player::getBet)
                .max()
                .orElse(0);
        if (highestCommittedBet == 0) {
            return false;
        }
        return players.stream()
                .filter(player -> !player.isFolded() && player.getChips() > 0)
                .anyMatch(player -> player.getBet() < highestCommittedBet);
    }

    public List<Player> getWinners() {
        if (gamePhase == GamePhase.SHOWDOWN) {
            List<Player> activePlayers = players.stream()
                    .filter(player -> !player.isFolded())
                    .toList();
            if (activePlayers.isEmpty()) {
                return List.of();
            }
            if (activePlayers.stream().anyMatch(player -> player.getHand() == null)) {
                throw new IllegalStateException("Showdown invariant violated: non-folded player without hand");
            }
            if (activePlayers.size() == 1) {
                return activePlayers;
            }

            Hand bestHand = activePlayers.stream()
                    .map(Player::getHand)
                    .max(Hand::compareTo)
                    .orElseThrow();
            return activePlayers.stream()
                    .filter(player -> player.getHand().compareTo(bestHand) == 0)
                    .toList();
        }
        if (gamePhase == GamePhase.FINISHED) {
            List<Player> activePlayers = players.stream()
                    .filter(player -> !player.isFolded())
                    .toList();
            if (activePlayers.size() != 1) {
                throw new IllegalStateException("Finished invariant violated: expected exactly one non-folded player");
            }
            return activePlayers;
        }
        return List.of();
    }

    public void addPlayer(String playerId, String displayName, int startChips) {
        if (playerExists(playerId)) {
            throw new IllegalStateException("Player already in game");
        }
        players.add(new Player(playerId, displayName, startChips));
    }

    public void validatePhase(Set<GamePhase> correctPhases) throws GameStateException {
        if (!correctPhases.contains(getGamePhase())) {
            throw new GameStateException("Action not legal in phase " + getGamePhase());
        }
    }

    public void validateCurrentPlayer(String playerId) throws GameStateException {
        Player player = getCurrentPlayer();
        if (player == null) {
            throw new GameStateException("No current player selected");
        }
        if (player.getId().equals(playerId)) {
            if (player.isReady()) {
                throw new GameStateException(String.format("Player %s is ready", player.getId()));
            }
            if (player.isFolded()) {
                throw new GameStateException(String.format("Player %s folded", player.getId()));
            }
        } else {
            throw new GameStateException("It's not player " + playerId + "turn");
        }
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.history.onPhaseChanged(this, gamePhase);

        this.gamePhase = gamePhase;
    }

    public int getPlayerIndex(String playerId) {
        if (!playerExists(playerId)) {
            throw new IllegalStateException("Player not found");
        }

        Player player = getPlayerById(playerId);
        return players.indexOf(player);
    }
}

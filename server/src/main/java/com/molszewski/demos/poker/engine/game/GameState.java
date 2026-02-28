package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.state.history.History;
import com.molszewski.demos.poker.engine.game.state.pot.PotState;
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
    private final PotState potState;

    public GameState(Deck deck) {
        this.deck = deck;
        this.players = new ArrayList<>();
        this.history = new History();
        this.potState = new PotState();
    }

    public GameState(Deck deck, List<Player> players) {
        this.deck = deck;
        this.players = new ArrayList<>(players);
        this.history = new History();
        this.potState = new PotState();
    }

    public Player getPlayerById(String id) {
        return players.stream().filter(player -> player.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Player with id " + id + " not found"));
    }

    public boolean playerExists(String id) {
        return players.stream().anyMatch(player -> player.getId().equals(id));
    }

    public void validatePhase(Set<GamePhase> correctPhases) throws ActionValidationException {
        if (!correctPhases.contains(getGamePhase())) {
            throw new ActionValidationException("Action not legal in phase " + getGamePhase());
        }
    }

    public void validateCurrentPlayer(String playerId) throws ActionValidationException {
        Player player = getCurrentPlayer();
        if (player == null) {
            throw new ActionValidationException("No current player selected");
        }
        if (player.getId().equals(playerId)) {
            if (player.isReady()) {
                throw new ActionValidationException(String.format("Player %s is ready", player.getId()));
            }
            if (player.isFolded()) {
                throw new ActionValidationException(String.format("Player %s folded", player.getId()));
            }
        } else {
            throw new ActionValidationException("It's not player " + playerId + "turn");
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

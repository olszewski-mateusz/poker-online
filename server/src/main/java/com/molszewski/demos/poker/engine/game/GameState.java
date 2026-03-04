package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.history.History;
import com.molszewski.demos.poker.engine.player.PlayerState;
import com.molszewski.demos.poker.engine.pot.PotState;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class GameState {
    private final Deck deck;
    private final PlayerState playerState;
    private GamePhase gamePhase = GamePhase.NOT_STARTED;

    private final History history;
    private final PotState potState;

    public GameState(Deck deck) {
        this.deck = deck;
        this.playerState = new PlayerState(new ArrayList<>());
        this.history = new History();
        this.potState = new PotState();
    }

    public GameState(Deck deck, List<Player> players) {
        this.deck = deck;
        this.playerState = new PlayerState(players);
        this.history = new History();
        this.potState = new PotState();
    }

    public void validatePhase(Set<GamePhase> correctPhases) throws ActionValidationException {
        if (!correctPhases.contains(getGamePhase())) {
            throw new ActionValidationException("Action not legal in phase " + getGamePhase());
        }
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.history.onPhaseChanged(this, gamePhase);

        this.gamePhase = gamePhase;
    }
}

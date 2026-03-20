package com.molszewski.demos.poker.engine.game;

import com.molszewski.demos.poker.engine.ai.AiState;
import com.molszewski.demos.poker.engine.deck.Deck;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.history.History;
import com.molszewski.demos.poker.engine.player.PlayerState;
import com.molszewski.demos.poker.engine.pot.PotState;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;

import java.util.*;

@Getter
public class GameState {
    private GamePhase gamePhase = GamePhase.NOT_STARTED;

    private final Deck deck;
    private final PlayerState playerState;
    private final History history;
    private final PotState potState;
    private final AiState aiState;

    public GameState(Deck deck) {
        this.deck = deck;
        this.playerState = new PlayerState(new ArrayList<>());
        this.history = new History();
        this.potState = new PotState();
        this.aiState = new AiState();
    }

    public GameState(Deck deck, List<Player> players) {
        this.deck = deck;
        this.playerState = new PlayerState(players);
        this.history = new History();
        this.potState = new PotState();
        this.aiState = new AiState();
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

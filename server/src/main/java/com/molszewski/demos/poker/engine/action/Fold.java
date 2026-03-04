package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;

import java.util.Set;

public final class Fold extends Action {

    public Fold(String playerId) {
        super(playerId);
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        PlayerState playerState = gameState.getPlayerState();

        gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING));
        if (!playerState.exists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        playerState.canPlayerMakeAction(getPlayerId());
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(gameState, this);
        Player player = gameState.getPlayerState().getById(this.getPlayerId());
        player.setFolded(true);
        ActionSteps.nextTurn(gameState);
    }
}

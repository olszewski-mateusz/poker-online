package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;

import java.util.Set;

public final class Call extends Action {
    public Call(String playerId) {
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

        Player player = playerState.getById(getPlayerId());
        int amountToCall = gameState.getPotState().getMaxContribution() - gameState.getPotState().getPlayerContribution(player);
        if (amountToCall == 0) {
            throw new ActionValidationException("No chips to call. Use check.");
        }
        if (amountToCall > player.getChips()) {
            throw new ActionValidationException("Not enough chips to call. Use all in or fold.");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(gameState, this);
        Player player = gameState.getPlayerState().getById(this.getPlayerId());
        player.setReady(true);
        gameState.getPotState().addCall(player);
        ActionSteps.nextTurn(gameState);
    }
}

package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;
import com.molszewski.demos.poker.engine.pot.PotState;

import java.util.Set;

public final class AllIn extends Action {
    public AllIn(String playerId) {
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
        PotState potState = gameState.getPotState();
        PlayerState playerState = gameState.getPlayerState();

        gameState.getHistory().addAction(gameState, this);
        Player player = playerState.getById(this.getPlayerId());

        int maxContribution = potState.getMaxContribution();
        int lastFullRaise = potState.getLastFullRaise();
        potState.addAllIn(player);
        int allInRaiseSize = potState.getMaxContribution() - maxContribution;
        if (allInRaiseSize > 0 && allInRaiseSize >= lastFullRaise) {
            playerState.removeReadyForAllWithChips();
        }
        player.setReady(true);
        ActionSteps.nextTurn(gameState);
    }
}

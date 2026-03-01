package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.Set;

public final class AllIn extends Action {
    public AllIn(String playerId) {
        super(playerId);
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING));
        if (!gameState.playerExists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        gameState.validateCurrentPlayer(getPlayerId());
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());

        int maxContribution = gameState.getPotState().getMaxContribution();
        int lastFullRaise = gameState.getPotState().getLastFullRaise();
        gameState.getPotState().addAllIn(player);
        int allInRaiseSize = gameState.getPotState().getMaxContribution() - maxContribution;
        if (allInRaiseSize > 0 && allInRaiseSize >= lastFullRaise) {
            gameState.getPlayers().forEach(p -> {
                if (p.getChips() > 0) {
                    p.setReady(false);
                }
            });
        }
        player.setReady(true);
        ActionSteps.nextTurn(gameState);
    }
}

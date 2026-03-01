package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.Set;

public final class Raise extends Action {

    private final int amount;

    public Raise(String playerId, int amount) {
        super(playerId);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING));
        if (!gameState.playerExists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        gameState.validateCurrentPlayer(getPlayerId());

        Player player = gameState.getPlayerById(this.getPlayerId());
        if (!gameState.getPotState().isBetPlaced()) {
            throw new ActionValidationException("No bet placed in this phase. Use bet.");
        }
        int minimumRaiseTo = gameState.getPotState().getMaxContribution() + gameState.getPotState().getLastFullRaise();
        if (amount < minimumRaiseTo) {
            throw new ActionValidationException("Raise must be at least " + minimumRaiseTo + " chips");
        }
        if (amount - gameState.getPotState().getPlayerContribution(player) > player.getChips()) {
            throw new ActionValidationException("Must have chips for raise.");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());
        gameState.getPlayers().forEach(p -> {
            if (p.getChips() > 0) {
                p.setReady(false);
            }
        });
        player.setReady(true);
        gameState.getPotState().addRaise(player, amount);
        ActionSteps.nextTurn(gameState);
    }
}

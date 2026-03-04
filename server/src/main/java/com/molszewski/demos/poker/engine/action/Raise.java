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
        PlayerState playerState = gameState.getPlayerState();
        PotState potState = gameState.getPotState();

        gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING));
        if (!playerState.exists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        playerState.canPlayerMakeAction(getPlayerId());

        Player player = playerState.getById(this.getPlayerId());
        if (!potState.isBetPlaced()) {
            throw new ActionValidationException("No bet placed in this phase. Use bet.");
        }
        int minimumRaiseTo = potState.getMaxContribution() + potState.getLastFullRaise();
        if (amount < minimumRaiseTo) {
            throw new ActionValidationException("Raise must be at least " + minimumRaiseTo + " chips");
        }
        if (amount - potState.getPlayerContribution(player) > player.getChips()) {
            throw new ActionValidationException("Must have chips for raise.");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        PlayerState playerState = gameState.getPlayerState();

        gameState.getHistory().addAction(gameState, this);
        Player player = playerState.getById(this.getPlayerId());
        playerState.removeReadyForAllWithChips();
        player.setReady(true);
        gameState.getPotState().addRaise(player, amount);
        ActionSteps.nextTurn(gameState);
    }
}

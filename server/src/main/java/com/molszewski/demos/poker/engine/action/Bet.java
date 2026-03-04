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

public final class Bet extends Action {
    private final int amount;

    public Bet(String playerId, int amount) {
        super(playerId);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        PlayerState playerState = gameState.getPlayerState();

        gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING));
        if (!playerState.exists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        playerState.canPlayerMakeAction(getPlayerId());

        Player player = playerState.getById(this.getPlayerId());
        if (gameState.getPotState().isBetPlaced()) {
            throw new ActionValidationException("Bet already placed in this phase. Use raise.");
        }
        int currentContribution = gameState.getPotState().getPlayerContribution(player);
        if (amount < currentContribution + configuration.minOpenBet()) {
            throw new ActionValidationException("Bet cannot be lower than current contribution.");
        }
        if (amount < configuration.minOpenBet()) {
            throw new ActionValidationException("Bet must be at least " + configuration.minOpenBet() + " chips");
        }
        if (amount - currentContribution > player.getChips()) {
            throw new ActionValidationException("Must have chips for bet.");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        PlayerState playerState = gameState.getPlayerState();

        gameState.getHistory().addAction(gameState, this);
        Player player = playerState.getById(this.getPlayerId());
        playerState.removeReadyForAllWithChips();
        player.setReady(true);
        gameState.getPotState().addBet(player, amount);
        ActionSteps.nextTurn(gameState);
    }
}

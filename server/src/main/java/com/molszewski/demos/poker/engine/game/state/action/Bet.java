package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;

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
    protected void validate(GameState gameState, GameConfiguration configuration) throws GameStateException {
        gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING));
        if (!gameState.playerExists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        gameState.validateCurrentPlayer(getPlayerId());

        Player player = gameState.getPlayerById(this.getPlayerId());
        int currentBet = gameState.getCurrentBet();
        if (gameState.isBetPlacedInCurrentPhase()) {
            throw new GameStateException("Bet already placed in this phase. Use raise.");
        }
        if (currentBet != player.getBet()) {
            throw new GameStateException("Must call current bet before betting.");
        }
        int minimumOpenTo = Math.max(configuration.minOpenBet(), currentBet + 1);
        if (amount < minimumOpenTo) {
            throw new GameStateException("Bet must be at least " + minimumOpenTo + " chips");
        }
        if (amount - player.getBet() > player.getChips()) {
            throw new GameStateException("Must have chips for bet.");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());
        int oldCurrentBet = gameState.getCurrentBet();
        gameState.getPlayers().forEach(p -> {
            if (p.getChips() > 0) {
                p.setReady(false);
            }
        });
        player.setReady(true);
        player.transferChipsToBet(amount - player.getBet());
        gameState.getHistory().registerFullRaiseSize(gameState.getCurrentBet() - oldCurrentBet);
        ActionFlow.nextTurn(gameState);
    }
}

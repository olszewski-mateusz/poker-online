package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.game.state.exception.PlayerNotFound;
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
    protected void validate(GameState gameState, GameConfiguration configuration) throws GameStateException {
        gameState.validatePhase(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING));
        if (!gameState.playerExists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        gameState.validateCurrentPlayer(getPlayerId());

        Player player = gameState.getPlayerById(this.getPlayerId());
        int currentBet = gameState.getCurrentBet();
        if (amount < 2 * currentBet) {
            throw new GameStateException("Must raise at least double of the current bet");
        }
        if (amount - player.getBet() > player.getMoney()) {
            throw new GameStateException("Must have money for raise.");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());
        gameState.getPlayers().forEach(p -> {
            if (p.getMoney() > 0) {
                p.setReady(false);
            }
        });
        player.setReady(true);
        player.transferMoneyToBet(amount - player.getBet());
        ActionFlow.nextTurn(gameState);
    }
}

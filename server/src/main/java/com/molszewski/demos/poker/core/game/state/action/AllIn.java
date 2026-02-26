package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.game.state.exception.GameStateException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

import java.util.Set;

public final class AllIn extends Action {
    public AllIn(String playerId) {
        super(playerId);
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws GameStateException {
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

        int oldBet = gameState.getCurrentBet();

        player.transferMoneyToBet(player.getMoney());

        int newBet = gameState.getCurrentBet();
        if (newBet > oldBet) {
            gameState.getPlayers().forEach(p -> {
                if (p.getMoney() > 0) {
                    p.setReady(false);
                }
            });
        }
        player.setReady(true);
        ActionFlow.nextTurn(gameState);
    }
}

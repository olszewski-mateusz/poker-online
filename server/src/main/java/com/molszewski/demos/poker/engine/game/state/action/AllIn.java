package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.player.Player;

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
        boolean aggressiveActionAlreadyPlaced = gameState.isBetPlacedInCurrentPhase();
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());

        int oldBet = gameState.getCurrentBet();

        player.transferChipsToBet(player.getChips());

        int newBet = gameState.getCurrentBet();
        int betIncrease = newBet - oldBet;
        int minimumRaiseTo = Raise.minimumRaiseTo(gameState, configuration, oldBet);
        boolean isFullRaise = betIncrease > 0
                && (!aggressiveActionAlreadyPlaced || newBet >= minimumRaiseTo);
        if (isFullRaise) {
            gameState.getHistory().registerFullRaiseSize(betIncrease);
            gameState.getPlayers().forEach(p -> {
                if (p.getChips() > 0) {
                    p.setReady(false);
                }
            });
        }
        player.setReady(true);
        ActionFlow.nextTurn(gameState);
    }
}

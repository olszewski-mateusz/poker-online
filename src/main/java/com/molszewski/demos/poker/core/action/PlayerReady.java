package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.GameContext;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.player.Player;

import java.util.UUID;

public class PlayerReady extends Action {

    protected PlayerReady(UUID playerId) {
        super(playerId);
    }

    @Override
    public void execute(GameContext gameContext) {
        gameContext.getPlayerById(playerId).setReady(true);
        if (gameContext.playerCount() >= gameContext.getConfiguration().minPlayersToStartGame()) {
            startGame(gameContext);
        }
    }

    private void startGame(GameContext gameContext) {
        gameContext.changeState(GameState.FIRST_BETTING);
        for (Player player : gameContext.getPlayers()) {
            player.setHand(gameContext.getDeck().getHand());
        }
    }
}

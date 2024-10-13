package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.GameContext;
import com.molszewski.demos.poker.core.game.GameException;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.player.Player;

import java.util.Set;
import java.util.UUID;

public class PlayerReady extends Action {

    protected PlayerReady(UUID playerId) {
        super(playerId);
    }

    @Override
    protected Set<GameState> legalStates() {
        return Set.of(GameState.NOT_STARTED);
    }

    @Override
    public void changeState(GameContext gameContext) throws GameException {
        gameContext.getPlayerById(this.getPlayerId()).setReady(true);
        long playersReady = gameContext.getPlayers().stream().filter(Player::isReady).count();
        if (playersReady >= gameContext.getConfiguration().minPlayersToStartGame() && playersReady == gameContext.getPlayers().size()) {
            startGame(gameContext);
        }
    }

    private void startGame(GameContext gameContext) {
        gameContext.changeState(GameState.FIRST_BETTING);
        gameContext.nextCurrentPlayer();
        for (Player player : gameContext.getPlayers()) {
            player.setHand(gameContext.getDeck().getHand());
        }
    }
}

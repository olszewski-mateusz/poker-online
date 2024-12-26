package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

public final class AllIn extends Action {
    public AllIn(String playerId) {
        super(playerId);
    }

    @Override
    public void execute(GameState gameState, GameConfiguration configuration) throws ActionException {
        Player player = gameState.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));

        int oldBid = gameState.getCurrentBet();

        player.moveMoneyToBet(player.getMoney());

        int newBid = gameState.getCurrentBet();
        if (newBid > oldBid) {
            gameState.getPlayers().forEach(p -> {
                if (p.getMoney() > 0) {
                    p.setReady(false);
                }
            });
        }
        player.setReady(true);
    }
}

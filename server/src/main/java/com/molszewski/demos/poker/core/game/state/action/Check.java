package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

public final class Check extends Action{
    public Check(String playerId) {
        super(playerId);
    }

    @Override
    public void execute(GameState gameState, GameConfiguration configuration) throws ActionException {
        Player player = gameState.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));
        player.setReady(true);
        int currentBet = gameState.getCurrentBet();
        if (currentBet - player.getBet() > player.getMoney()) {
            throw new ActionException("Not having money for check. Use all in or fold.");
        }
        player.transferMoneyToBet(currentBet - player.getBet());
    }
}

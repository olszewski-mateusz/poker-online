package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

public final class Check extends Action{
    public Check(String playerId) {
        super(playerId);
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        Player player = board.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));
        player.setReady(true);
        int currentBid = board.getCurrentBid();
        if (currentBid - player.getBid() > player.getMoney()) {
            throw new ActionException("Not having money for check. Use all in or fold.");
        }
        player.moveMoneyToBid(currentBid - player.getBid());
    }
}

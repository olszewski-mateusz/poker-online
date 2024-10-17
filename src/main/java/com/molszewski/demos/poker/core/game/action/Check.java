package com.molszewski.demos.poker.core.game.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.player.Player;

import java.util.UUID;

public final class Check extends Action{
    public Check(UUID playerId) {
        super(playerId);
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        Player player = board.getPlayerById(getPlayerId());
        player.setReady(true);
        int currentBid = board.getCurrentBid();
        if (currentBid - player.getBid() > player.getMoney()) {
            throw new ActionException("Not having money for check. Use all in or fold.");
        }
        player.moveMoneyToBid(currentBid - player.getBid());
    }
}

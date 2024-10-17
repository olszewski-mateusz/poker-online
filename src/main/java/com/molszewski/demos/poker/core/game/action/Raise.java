package com.molszewski.demos.poker.core.game.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.player.Player;

import java.util.UUID;

public final class Raise extends Action {

    private final int amount;

    public Raise(UUID playerId, int amount) {
        super(playerId);
        this.amount = amount;
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        Player player = board.getPlayerById(getPlayerId());
        board.getPlayers().stream().filter(p -> !p.isFolded()).forEach(p -> p.setReady(false));
        player.setReady(true);

        int currentBid = board.getCurrentBid();
        if (amount < 2 * currentBid) {
            throw new ActionException("Must raise to at least double the current bid");
        }
        if (amount - player.getBid() > player.getMoney()) {
            throw new ActionException("Not having money for raise.");
        }
        player.moveMoneyToBid(amount - player.getBid());
    }
}

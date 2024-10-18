package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

public final class Raise extends Action {

    private final int amount;

    public Raise(String playerId, int amount) {
        super(playerId);
        this.amount = amount;
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        Player player = board.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));
        board.getPlayers().stream().filter(p -> !p.isFolded()).forEach(p -> p.setReady(false));
        player.setReady(true);

        int currentBid = board.getCurrentBid();
        if (amount < 2 * currentBid) {
            throw new ActionException("Must raise at least double of the current bid");
        }
        if (amount - player.getBid() > player.getMoney()) {
            throw new ActionException("Must have money for raise.");
        }
        player.moveMoneyToBid(amount - player.getBid());
    }
}

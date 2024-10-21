package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.player.Player;

public final class AllIn extends Action {
    public AllIn(String playerId) {
        super(playerId);
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        Player player = board.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));

        int oldBid = board.getCurrentBet();

        player.moveMoneyToBet(player.getMoney());

        int newBid = board.getCurrentBet();
        if (newBid > oldBid) {
            board.getPlayers().stream().filter(p -> !p.isFolded()).forEach(p -> p.setReady(false));
        }
        player.setReady(true);
    }
}

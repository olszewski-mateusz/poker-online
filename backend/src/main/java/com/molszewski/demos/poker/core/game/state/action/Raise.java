package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.game.GameState;
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
    public void execute(GameState gameState, GameConfiguration configuration) throws ActionException {
        Player player = gameState.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));
        gameState.getPlayers().forEach(p -> {
            if (p.getMoney() > 0) {
                p.setReady(false);
            }
        });
        player.setReady(true);

        int currentBet = gameState.getCurrentBet();
        if (amount < 2 * currentBet) {
            throw new ActionException("Must raise at least double of the current bet");
        }
        if (amount - player.getBet() > player.getMoney()) {
            throw new ActionException("Must have money for raise.");
        }
        player.moveMoneyToBet(amount - player.getBet());
    }
}

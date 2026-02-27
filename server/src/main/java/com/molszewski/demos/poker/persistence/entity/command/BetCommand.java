package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.action.Bet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class BetCommand extends Command {
    private int amount;

    @Override
    public Action toAction() {
        return new Bet(this.getPlayerId(), this.amount);
    }

    BetCommand(final String playerId, final int amount) {
        super(playerId);
        this.amount = amount;
    }
}

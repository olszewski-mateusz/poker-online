package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.action.AllIn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class AllInCommand extends Command {
    @Override
    public Action toAction() {
        return new AllIn(this.getPlayerId());
    }

    AllInCommand(final String playerId) {
        super(playerId);
    }
}

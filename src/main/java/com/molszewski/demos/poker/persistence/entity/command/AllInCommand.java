package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.AllIn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.AllIn;
import com.molszewski.demos.poker.core.game.state.action.Check;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
public final class CheckCommand extends Command {
    @Override
    public Action toAction() {
        return new Check(this.getPlayerId());
    }

    CheckCommand(final String playerId) {
        super(playerId);
    }
}

package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.action.Check;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

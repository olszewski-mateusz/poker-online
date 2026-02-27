package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.game.state.action.Action;
import com.molszewski.demos.poker.engine.game.state.action.Call;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class CallCommand extends Command {
    @Override
    public Action toAction() {
        return new Call(this.getPlayerId());
    }

    CallCommand(final String playerId) {
        super(playerId);
    }
}

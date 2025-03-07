package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Fold;
import com.molszewski.demos.poker.core.game.state.action.Ready;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
public final class ReadyCommand extends Command {
    private boolean ready;

    @Override
    public Action toAction() {
        return new Ready(this.getPlayerId(), ready);
    }

    ReadyCommand(final String playerId, final boolean ready) {
        super(playerId);
        this.ready = ready;
    }
}

package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.action.Join;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class JoinCommand extends Command {
    private String displayName;

    @Override
    public Action toAction() {
        return new Join(this.getPlayerId(), this.displayName);
    }

    JoinCommand(final String playerId, final String displayName) {
        super(playerId);
        this.displayName = displayName;
    }
}

package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Fold;
import com.molszewski.demos.poker.core.game.state.action.Join;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
public final class JoinCommand extends Command {
    private String displayName;

    @Override
    public Action toAction() {
        return new Join(this.getPlayerId());
    }

    JoinCommand(final String playerId, final String displayName) {
        super(playerId);
        this.displayName = displayName;
    }
}

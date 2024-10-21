package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Check;
import com.molszewski.demos.poker.core.game.state.action.Fold;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
public final class FoldCommand extends Command {
    @Override
    public Action toAction() {
        return new Fold(this.getPlayerId());
    }

    FoldCommand(final String playerId) {
        super(playerId);
    }
}

package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.action.Fold;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.action.RemovePlayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class RemovePlayerCommand extends Command {
    private int playerIndex;

    @Override
    public Action toAction() {
        return new RemovePlayer(this.getPlayerId(), this.playerIndex);
    }

    RemovePlayerCommand(final String playerId, final int playerIndex) {
        super(playerId);
        this.playerIndex = playerIndex;
    }
}

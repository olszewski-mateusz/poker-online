package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.engine.action.Action;
import com.molszewski.demos.poker.engine.action.AiJoin;
import com.molszewski.demos.poker.engine.ai.AiConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class AiJoinCommand extends Command {
    private String displayName;

    @Override
    public Action toAction() {
        return new AiJoin(this.getPlayerId(), this.displayName, new AiConfiguration(AiConfiguration.Aggressivity.MEDIUM));
    }

    AiJoinCommand(final String playerId, final String displayName) {
        super(playerId);
        this.displayName = displayName;
    }
}

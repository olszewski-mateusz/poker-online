package com.molszewski.demos.poker.persistence.entity.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public final class JoinCommand extends Command {
    private String displayName;
}

package com.molszewski.demos.poker.persistence.entity.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public final class RaiseCommand extends Command {
    private int amount;
}

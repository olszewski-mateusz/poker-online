package com.molszewski.demos.poker.persistence.entity.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Command permits AllInCommand, CheckCommand, FoldCommand, JoinCommand, RaiseCommand, ReadyCommand, ReplaceCommand {
    private String playerId;
    private String timestamp = LocalDateTime.now().toString();
}

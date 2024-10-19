package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.card.Card;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@NoArgsConstructor
@SuperBuilder
public final class ReplaceCommand extends Command {
    private Set<Card> cardsToReplace;
}

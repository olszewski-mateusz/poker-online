package com.molszewski.demos.poker.persistence.entity.command;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.game.state.action.Action;
import com.molszewski.demos.poker.core.game.state.action.Ready;
import com.molszewski.demos.poker.core.game.state.action.Replace;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public final class ReplaceCommand extends Command {
    @Nullable
    private Set<Card> cardsToReplace;

    @Override
    public Action toAction() {
        return new Replace(this.getPlayerId(), Optional.ofNullable(this.cardsToReplace).orElse(Set.of()));
    }

    ReplaceCommand(final String playerId, @Nullable final Set<Card> cardsToReplace) {
        super(playerId);
        this.cardsToReplace = cardsToReplace;
    }

    public Set<Card> getCardsToReplace() {
        return Optional.ofNullable(this.cardsToReplace).orElse(Set.of());
    }
}

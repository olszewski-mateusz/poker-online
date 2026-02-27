package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.hand.HandType;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Builder;

import java.util.Optional;
import java.util.Set;

@Builder
public record PlayerResponse(
        int index,
        String name,
        int chips,
        int bet,
        boolean ready,
        boolean folded,
        Set<Card> cards,
        HandType handType
) {
    public static PlayerResponse fromPlayer(Player player, int index, boolean hideCards) {
        return  PlayerResponse.builder()
                .index(index)
                .name(player.getName())
                .chips(player.getChips())
                .bet(player.getBet())
                .ready(player.isReady())
                .folded(player.isFolded())
                .cards(hideCards ? null : Optional.ofNullable(player.getHand()).map(Hand::getCards).orElse(null))
                .handType(hideCards ? null : Optional.ofNullable(player.getHand()).map(Hand::getHandType).orElse(null))
                .build();
    }
}

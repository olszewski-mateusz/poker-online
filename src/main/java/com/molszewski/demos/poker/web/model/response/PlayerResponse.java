package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.hand.Hand;
import com.molszewski.demos.poker.core.hand.HandType;
import com.molszewski.demos.poker.core.player.Player;
import lombok.Builder;

import java.util.Optional;
import java.util.Set;

@Builder
public record PlayerResponse(
        String id,
        int money,
        int bid,
        boolean ready,
        boolean folded,
        Set<Card> cards,
        HandType handType
) {
    public static PlayerResponse fromPlayer(Player player) {
        return  PlayerResponse.builder()
                .id(player.getId())
                .money(player.getMoney())
                .bid(player.getBid())
                .ready(player.isReady())
                .folded(player.isFolded())
                .cards(Optional.ofNullable(player.getHand()).map(Hand::getCards).orElse(null))
                .handType(Optional.ofNullable(player.getHand()).map(Hand::getHandType).orElse(null))
                .build();
    }
}

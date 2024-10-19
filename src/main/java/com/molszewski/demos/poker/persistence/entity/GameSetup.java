package com.molszewski.demos.poker.persistence.entity;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.deck.Deck;
import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.game.state.StateManagerImpl;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Builder
public record GameSetup(
        String id,
        List<Card> deck,
        GameConfiguration configuration
) {
    public static GameSetup init(String gameId, Random random) {
        List<Card> cards = new ArrayList<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(cards, random);
        return GameSetup.builder()
                .id(gameId)
                .configuration(GameConfiguration.defaultConfiguration())
                .deck(cards)
                .build();
    }

    public Game toGame(Random random) {
        Board board = new Board(new Deck(deck, List.of(), random));
        StateManager stateManager = new StateManagerImpl();
        return new Game(board, stateManager, configuration);
    }
}

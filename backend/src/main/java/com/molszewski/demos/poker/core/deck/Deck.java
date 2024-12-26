package com.molszewski.demos.poker.core.deck;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.hand.Hand;

import java.util.*;

public class Deck {
    private List<Card> cards = new ArrayList<>();
    private List<Card> discards = new ArrayList<>();
    private final Random random;

    public Deck(Random random) {
        this.random = random;
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(cards, random);
    }

    public Deck(List<Card> cards, List<Card> discards, Random random) {
        this.cards = new ArrayList<>(cards);
        this.discards = new ArrayList<>(discards);
        this.random = random;
    }

    public Card pop() {
        if (cards.isEmpty()) {
            moveDiscardsToDeck();
            if (cards.isEmpty()) {
                throw new IllegalStateException("Deck is empty");
            }
        }
        return cards.removeLast();
    }

    public Hand getHand() {
        Set<Card> handCards = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            handCards.add(pop());
        }
        return new Hand(handCards);
    }

    private void moveDiscardsToDeck() {
        Collections.shuffle(discards, random);
        cards.addAll(discards);
    }

    public void addAllToDiscards(Collection<Card> cards) {
        discards.addAll(cards);
    }

    public void addToDiscards(Card card) {
        discards.add(card);
    }

    public int cardsCount() {
        return cards.size();
    }

    public int discardsCount() {
        return discards.size();
    }

    public List<Card> getDiscards() {
        return List.copyOf(discards);
    }

    public List<Card> getCards() {
        return List.copyOf(cards);
    }
}

package com.molszewski.demos.poker.core.deck;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.core.hand.Hand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    private final Random random = new Random(17L);
    private List<Card> sixCardsList;
    private List<Card> twoCardsList;

    @BeforeEach
    void setUp() {
        sixCardsList = List.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS),
                new Card(Rank.FOUR, Suit.SPADES),
                new Card(Rank.FIVE, Suit.CLUBS),
                new Card(Rank.SIX, Suit.SPADES),
                new Card(Rank.SEVEN, Suit.DIAMONDS)
        );

        twoCardsList = List.of(
                new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.KING, Suit.HEARTS)
        );
    }

    @Test
    @DisplayName("Correctly pop one card from deck")
    void simplePop() {
        Deck deck = new Deck(sixCardsList, twoCardsList, random);
        assertEquals( 6, deck.getCards().size());
        assertEquals( 2, deck.getDiscards().size());

        Card card = deck.pop();

        assertEquals( 5, deck.getCards().size());
        assertEquals( 2, deck.getDiscards().size());
        assertEquals(Rank.SEVEN, card.rank());
        assertEquals(Suit.DIAMONDS, card.suit());
    }

    @Test
    @DisplayName("Pop card from deck without cards with discards")
    void popOnEmptyDeckWithDiscards() {
        Deck deck = new Deck(List.of(), twoCardsList, random);
        assertEquals( 0, deck.getCards().size());
        assertEquals( 2, deck.getDiscards().size());

        Card card = deck.pop();

        assertEquals( 1, deck.getCards().size());
        assertEquals( 0, deck.getDiscards().size());
        assertTrue(twoCardsList.contains(card));
    }

    @Test
    @DisplayName("Pop card from deck without cards without discards - throws error")
    void popOnEmptyDeckWithoutDiscards() {
        Deck deck = new Deck(List.of(), List.of(), random);
        assertEquals( 0, deck.getCards().size());
        assertEquals( 0, deck.getDiscards().size());

        assertThrows(IllegalStateException.class, () -> deck.pop());
    }

    @Test
    @DisplayName("Correctly get hand from deck")
    void simpleGetHand() {
        Deck deck = new Deck(sixCardsList, twoCardsList, random);
        assertEquals( 6, deck.getCards().size());
        assertEquals( 2, deck.getDiscards().size());

        Hand hand = deck.getHand();

        assertEquals( 1, deck.getCards().size());
        assertEquals( 2, deck.getDiscards().size());
        assertEquals(5, hand.getCards().size());
    }

    @Test
    @DisplayName("Correctly add cards do discards")
    void addAllToDiscards() {
        Deck deck = new Deck(sixCardsList, twoCardsList, random);
        assertEquals( 6, deck.getCards().size());
        assertEquals( 2, deck.getDiscards().size());

        deck.addAllToDiscards(List.of(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS)
        ));

        assertEquals( 6, deck.getCards().size());
        assertEquals( 4, deck.getDiscards().size());
    }
}
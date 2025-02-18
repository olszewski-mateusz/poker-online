package com.molszewski.demos.poker.core.hand;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
class HandTest {

    @Test
    @DisplayName("Hand - getCards correct")
    void getCardsTest() {
        Hand hand = new Hand(
                Set.of(
                        new Card(Rank.TWO, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.DIAMONDS),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.TWO, Suit.HEARTS),
                        new Card(Rank.EIGHT, Suit.CLUBS)
                )
        );

        assertEquals(5, hand.getCards().size());
    }

    @Test
    @DisplayName("Hand - wrong number of cards - throws error")
    void getCardsTestWrong() {
        Set<Card> cards = Set.of(
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.JACK, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.CLUBS)
        );

        assertThrows(IllegalArgumentException.class, () -> new Hand(cards));
    }

    @Test
    @DisplayName("Hand - getHandType correct")
    void getHandTypeTest() {
        Hand hand = new Hand(
                Set.of(
                        new Card(Rank.TWO, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.DIAMONDS),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.TWO, Suit.HEARTS),
                        new Card(Rank.EIGHT, Suit.CLUBS)
                )
        );

        assertEquals(HandType.PAIR, hand.getHandType());
    }

    @Test
    @DisplayName("Hand - compareTo correct")
    void compareToTestPositive() {
        Hand handA = new Hand(
                Set.of(
                        new Card(Rank.TWO, Suit.CLUBS),
                        new Card(Rank.THREE, Suit.DIAMONDS),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.FOUR, Suit.HEARTS),
                        new Card(Rank.SIX, Suit.CLUBS)
                )
        );

        Hand handB = new Hand(
                Set.of(
                        new Card(Rank.TWO, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.DIAMONDS),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.TWO, Suit.HEARTS),
                        new Card(Rank.EIGHT, Suit.CLUBS)
                )
        );

        assertTrue(handA.compareTo(handB) > 0);
    }

    @Test
    @DisplayName("Hand - compareTo correct")
    void compareToTestNegative() {
        Hand handA = new Hand(
                Set.of(
                        new Card(Rank.KING, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.SPADES),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.ACE, Suit.CLUBS),
                        new Card(Rank.QUEEN, Suit.HEARTS)
                )
        );

        Hand handB = new Hand(
                Set.of(
                        new Card(Rank.TWO, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.DIAMONDS),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.TWO, Suit.HEARTS),
                        new Card(Rank.EIGHT, Suit.CLUBS)
                )
        );

        assertTrue(handA.compareTo(handB) < 0);
    }

    @Test
    @DisplayName("Hand - compareTo correct")
    void compareToTestZero() {
        Hand handA = new Hand(
                Set.of(
                        new Card(Rank.KING, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.SPADES),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.ACE, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.HEARTS)
                )
        );

        Hand handB = new Hand(
                Set.of(
                        new Card(Rank.ACE, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.DIAMONDS),
                        new Card(Rank.FIVE, Suit.CLUBS),
                        new Card(Rank.TWO, Suit.HEARTS),
                        new Card(Rank.JACK, Suit.CLUBS)
                )
        );

        assertEquals(0, handA.compareTo(handB));
    }
}
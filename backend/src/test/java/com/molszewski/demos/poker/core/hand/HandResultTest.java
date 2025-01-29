package com.molszewski.demos.poker.core.hand;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;
import com.molszewski.demos.poker.core.card.Suit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandResultTest {

    @Test
    @DisplayName("Correct hand - high card")
    void highCardTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS)
        ));

        assertEquals(HandType.HIGH_CARD, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - pair")
    void pairTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS)
        ));

        assertEquals(HandType.PAIR, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - two pairs")
    void twoPairTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.EIGHT, Suit.CLUBS),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS)
        ));

        assertEquals(HandType.TWO_PAIR, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - three of a kind")
    void threeOfAKindTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.JACK, Suit.DIAMONDS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS)
        ));

        assertEquals(HandType.THREE_OF_A_KIND, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - straight")
    void straightTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.CLUBS)
        ));

        assertEquals(HandType.STRAIGHT, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - flush")
    void flushTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.JACK, Suit.DIAMONDS),
                new Card(Rank.SEVEN, Suit.DIAMONDS),
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.TWO, Suit.DIAMONDS)
        ));

        assertEquals(HandType.FLUSH, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - full house")
    void fullHouseTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.EIGHT, Suit.DIAMONDS),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.EIGHT, Suit.CLUBS)
        ));

        assertEquals(HandType.FULL_HOUSE, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - four of a kind")
    void fourOfAKindTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.JACK, Suit.DIAMONDS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.JACK, Suit.SPADES)
        ));

        assertEquals(HandType.FOUR_OF_A_KIND, result.getHandType());
    }

    @Test
    @DisplayName("Correct hand - straight flush")
    void straightFlushTest() {
        HandResult result = new HandResult(Set.of(
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        assertEquals(HandType.STRAIGHT_FLUSH, result.getHandType());
    }

    @Test
    @DisplayName("Compare hands - different types")
    void compareDifferentHandTypes() {
        HandResult resultA = new HandResult(Set.of(
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        HandResult resultB = new HandResult(Set.of(
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        HandResult resultC = new HandResult(Set.of(
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        assertTrue(resultA.compareTo(resultB) < 0);
        assertTrue(resultB.compareTo(resultC) < 0);
        assertTrue(resultA.compareTo(resultC) < 0);
    }

    @Test
    @DisplayName("Compare hands - same types and score")
    void compareSameHandTypesSameScore() {
        HandResult resultA = new HandResult(Set.of(
                new Card(Rank.TWO, Suit.SPADES),
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.TWO, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.SPADES),
                new Card(Rank.FIVE, Suit.HEARTS)
        ));

        HandResult resultB = new HandResult(Set.of(
                new Card(Rank.FIVE, Suit.SPADES),
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.TWO, Suit.SPADES),
                new Card(Rank.FIVE, Suit.HEARTS)
        ));

        assertEquals(0, resultA.compareTo(resultB));
    }

    @Test
    @DisplayName("Compare hands - same types and different score")
    void compareSameHandTypesDifferentScore() {
        HandResult resultA = new HandResult(Set.of(
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.TWO, Suit.SPADES),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.SPADES),
                new Card(Rank.FIVE, Suit.HEARTS)
        ));

        HandResult resultB = new HandResult(Set.of(
                new Card(Rank.SIX, Suit.SPADES),
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.SIX, Suit.HEARTS)
        ));

        HandResult resultC = new HandResult(Set.of(
                new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.THREE, Suit.HEARTS),
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.SIX, Suit.SPADES)
        ));

        assertTrue(resultA.compareTo(resultB) < 0);
        assertTrue(resultB.compareTo(resultC) < 0);
        assertTrue(resultA.compareTo(resultC) < 0);
    }
}
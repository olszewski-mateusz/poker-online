package com.molszewski.demos.poker.core.hand;


import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.card.Rank;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class HandResult {
    private final HandType handType;
    private final Rank highestScoringRank;
    private final Rank secondHighestScoringRank;

    HandResult(Set<Card> cards) {
        if (cards.size() != 5) {
            throw new IllegalArgumentException("Hand must have 5 Cards");
        }

        boolean color = isColor(cards);
        List<Rank> sortedRanks = sortByRank(cards);
        boolean straight = isStraight(sortedRanks);
        List<Map.Entry<Rank, Long>> rankCounts = countRanks(sortedRanks);

        Map.Entry<Rank, Long> mostOccurringRank = rankCounts.removeLast();
        highestScoringRank = mostOccurringRank.getKey();

        Map.Entry<Rank, Long> secondMostOccurringRank = rankCounts.removeLast();
        secondHighestScoringRank = secondMostOccurringRank.getKey();

        if (color && straight) {
            handType = HandType.STRAIGHT_FLUSH;
        } else if (mostOccurringRank.getValue() == 4) {
            handType = HandType.FOUR_OF_A_KIND;
        } else if (mostOccurringRank.getValue() == 3 && secondMostOccurringRank.getValue() == 2) {
            handType = HandType.FULL_HOUSE;
        } else if (color) {
            handType = HandType.FLUSH;
        } else if (straight) {
            handType = HandType.STRAIGHT;
        } else if (mostOccurringRank.getValue() == 3) {
            handType = HandType.THREE_OF_A_KIND;
        } else if (mostOccurringRank.getValue() == 2 && secondMostOccurringRank.getValue() == 2) {
            handType = HandType.TWO_PAIR;
        } else if (mostOccurringRank.getValue() == 2) {
            handType = HandType.PAIR;
        } else {
            handType = HandType.HIGH_CARD;
        }
    }

    HandType getHandType() {
        return handType;
    }

    int compareTo(HandResult otherResult) {
        if (handType.getValue() != otherResult.handType.getValue()) {
            return handType.getValue() - otherResult.handType.getValue();
        }

        if (highestScoringRank.getValue() != otherResult.highestScoringRank.getValue()) {
            return highestScoringRank.getValue() - otherResult.highestScoringRank.getValue();
        }

        if (handType.equals(HandType.TWO_PAIR) || handType.equals(HandType.FULL_HOUSE)) {
            return secondHighestScoringRank.getValue() - otherResult.secondHighestScoringRank.getValue();
        }

        return 0;
    }

    private boolean isColor(Set<Card> cards) {
        return cards.stream().map(Card::suit).distinct().count() == 1;
    }

    private List<Rank> sortByRank(Set<Card> cards) {
        return cards.stream()
                .map(Card::rank)
                .sorted(Comparator.comparingInt(Rank::getValue))
                .toList();
    }

    private boolean isStraight(List<Rank> sortedRanks) {
        for (int i = 0; i < 4; i++) {
            if (sortedRanks.get(i).getValue() != sortedRanks.get(i + 1).getValue() - 1) {
                return false;
            }
        }
        return true;
    }

    private List<Map.Entry<Rank, Long>> countRanks(List<Rank> sortedRanks) {
        return new ArrayList<>(sortedRanks.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted((o1, o2) -> {
                    if (Objects.equals(o1.getValue(), o2.getValue())) {
                        return o1.getKey().getValue() - o2.getKey().getValue();
                    } else {
                        return Math.toIntExact(o1.getValue() - o2.getValue());
                    }
                }).toList());
    }
}

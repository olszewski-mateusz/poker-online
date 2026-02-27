package com.molszewski.demos.poker.engine.hand;


import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.card.Rank;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class HandResult {
    private final HandType handType;
    private final List<Integer> scoreRanks;

    HandResult(Set<Card> cards) {
        if (cards.size() != 5) {
            throw new IllegalArgumentException("Hand must have 5 Cards");
        }

        boolean color = isColor(cards);
        List<Integer> sortedValues = sortByRankValue(cards);
        boolean straight = isStraight(sortedValues);
        List<Map.Entry<Rank, Long>> rankCounts = countRanks(cards);

        long highestGroupCount = rankCounts.getFirst().getValue();
        long secondHighestGroupCount = rankCounts.size() > 1 ? rankCounts.get(1).getValue() : 0L;

        if (color && straight) {
            handType = HandType.STRAIGHT_FLUSH;
            scoreRanks = List.of(straightHighCard(sortedValues));
        } else if (highestGroupCount == 4) {
            handType = HandType.FOUR_OF_A_KIND;
            scoreRanks = buildGroupedScore(rankCounts);
        } else if (highestGroupCount == 3 && secondHighestGroupCount == 2) {
            handType = HandType.FULL_HOUSE;
            scoreRanks = buildGroupedScore(rankCounts);
        } else if (color) {
            handType = HandType.FLUSH;
            scoreRanks = highCardScore(sortedValues);
        } else if (straight) {
            handType = HandType.STRAIGHT;
            scoreRanks = List.of(straightHighCard(sortedValues));
        } else if (highestGroupCount == 3) {
            handType = HandType.THREE_OF_A_KIND;
            scoreRanks = buildGroupedScore(rankCounts);
        } else if (highestGroupCount == 2 && secondHighestGroupCount == 2) {
            handType = HandType.TWO_PAIR;
            scoreRanks = buildGroupedScore(rankCounts);
        } else if (highestGroupCount == 2) {
            handType = HandType.PAIR;
            scoreRanks = buildGroupedScore(rankCounts);
        } else {
            handType = HandType.HIGH_CARD;
            scoreRanks = highCardScore(sortedValues);
        }
    }

    HandType getHandType() {
        return handType;
    }

    int compareTo(HandResult otherResult) {
        int handTypeCompare = Integer.compare(handType.getValue(), otherResult.handType.getValue());
        if (handTypeCompare != 0) {
            return handTypeCompare;
        }

        for (int i = 0; i < Math.min(scoreRanks.size(), otherResult.scoreRanks.size()); i++) {
            int compare = Integer.compare(scoreRanks.get(i), otherResult.scoreRanks.get(i));
            if (compare != 0) {
                return compare;
            }
        }

        return 0;
    }

    private boolean isColor(Set<Card> cards) {
        return cards.stream().map(Card::suit).distinct().count() == 1;
    }

    private List<Integer> sortByRankValue(Set<Card> cards) {
        return cards.stream()
                .map(card -> card.rank().getValue())
                .sorted()
                .toList();
    }

    private boolean isStraight(List<Integer> sortedValues) {
        if (isWheelStraight(sortedValues)) {
            return true;
        }
        for (int i = 0; i < 4; i++) {
            if (sortedValues.get(i) + 1 != sortedValues.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean isWheelStraight(List<Integer> sortedValues) {
        return sortedValues.equals(List.of(
                Rank.TWO.getValue(),
                Rank.THREE.getValue(),
                Rank.FOUR.getValue(),
                Rank.FIVE.getValue(),
                Rank.ACE.getValue()
        ));
    }

    private int straightHighCard(List<Integer> sortedValues) {
        if (isWheelStraight(sortedValues)) {
            return Rank.FIVE.getValue();
        }
        return sortedValues.getLast();
    }

    private List<Integer> highCardScore(List<Integer> sortedValues) {
        List<Integer> descending = new ArrayList<>(sortedValues);
        descending.sort(Comparator.reverseOrder());
        return descending;
    }

    private List<Map.Entry<Rank, Long>> countRanks(Set<Card> cards) {
        return cards.stream()
                .map(Card::rank)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted((left, right) -> {
                    int byCount = Long.compare(right.getValue(), left.getValue());
                    if (byCount != 0) {
                        return byCount;
                    }
                    return Integer.compare(right.getKey().getValue(), left.getKey().getValue());
                })
                .toList();
    }

    private List<Integer> buildGroupedScore(List<Map.Entry<Rank, Long>> rankCounts) {
        List<Integer> score = new ArrayList<>();
        for (Map.Entry<Rank, Long> rankCount : rankCounts) {
            for (int i = 0; i < rankCount.getValue(); i++) {
                score.add(rankCount.getKey().getValue());
            }
        }
        return score;
    }
}

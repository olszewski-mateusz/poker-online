package com.molszewski.demos.poker.engine.game.state.pot;

import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class PotState {
    private int lastFullRaise = 0;
    private final List<Integer> smallAllInCaps = new ArrayList<>();
    private final Map<String, Integer> contributions =  new HashMap<>();

    public void resetToNewRound() {
        lastFullRaise = 0;
        contributions.clear();
        smallAllInCaps.clear();
    }

    public void resetToNewBettingPhase() {
        lastFullRaise = 0;
    }

    private void addToContribution(Player player, int amount) {
        int currentContribution = getPlayerContribution(player);
        contributions.put(player.getId(), currentContribution + amount);
    }

    public void removePot(Pot pot) {
        pot.contributions().forEach((playerId, value) -> {
            int currentContribution = getPlayerContribution(playerId);
            if (value == currentContribution) {
                contributions.remove(playerId);
            } else {
                contributions.put(playerId, currentContribution - value);
            }
        });
    }

    public void addAnte(Player player, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (getPlayerContribution(player) != 0) {
            throw new IllegalArgumentException("Current contribution must be 0");
        }
        player.takeChips(amount);
        addToContribution(player, amount);
    }

    public void addBet(Player player, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        int betContribution = amount - getPlayerContribution(player);
        player.takeChips(betContribution);
        addToContribution(player, betContribution);

        if (lastFullRaise != 0) {
            throw new IllegalArgumentException("Last full raise must be equal to zero");
        }

        lastFullRaise = amount;
    }

    public void addRaise(Player player, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        int raiseSize = amount - getMaxContribution();
        if (raiseSize < lastFullRaise) {
            throw new IllegalArgumentException("New raise must be equal or greater than last full raise");
        }

        int raiseContribution = amount - getPlayerContribution(player);
        player.takeChips(raiseContribution);
        addToContribution(player, raiseContribution);

        lastFullRaise = raiseSize;
    }

    public void addAllIn(Player player) {
        int allInContribution = player.takeAllChips();
        int oldMaxContribution = getMaxContribution();
        addToContribution(player, allInContribution);
        int playerContribution = getPlayerContribution(player);
        int raiseSize = getPlayerContribution(player) - oldMaxContribution;
        if (!smallAllInCaps.contains(playerContribution)) {
            smallAllInCaps.add(playerContribution);
        }
        if (raiseSize >= lastFullRaise) {
            lastFullRaise = raiseSize;
        }
    }

    public void addCall(Player player) {
        int callContribution = getMaxContribution() - getPlayerContribution(player);
        player.takeChips(callContribution);
        addToContribution(player, callContribution);
    }

    public int getAmount() {
        return contributions.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<String, Integer> getPlayerContributions() {
        return new HashMap<>(contributions);
    }

    public List<Pot> calculatePots(List<Player> players) {
        List<Pot> pots = new ArrayList<>();
        Map<String, Integer> contributionsCopy = new HashMap<>(contributions);
        List<Integer> caps = smallAllInCaps.stream()
                .distinct()
                .sorted()
                .toList();

        int previousCap = 0;
        for (int cap : caps) {
            int layerWidth = cap - previousCap;
            if (layerWidth <= 0) {
                continue;
            }

            Map<String, Integer> potContributions = new HashMap<>();

            contributionsCopy.forEach((key, value) -> {
                if (value > 0) {
                    potContributions.put(key, Math.min(layerWidth, value));
                }
            });

            potContributions.forEach((key, value) -> {
               contributionsCopy.put(key, contributionsCopy.get(key) - value);
            });

            if (potContributions.isEmpty()) {
                previousCap = cap;
                continue;
            }

            Pot pot = new Pot(
                    pots.isEmpty() ? PotType.MAIN : PotType.SIDE,
                    potContributions,
                    players.stream()
                            .filter(player -> !player.isFolded() && potContributions.containsKey(player.getId()))
                            .map(Player::getId)
                            .collect(Collectors.toSet())
            );

            if (pot.eligible().isEmpty()) {
                throw  new IllegalArgumentException("There should always be at least one eligible");
            }

            pots.add(pot);
            previousCap = cap;
        }

        Map<String, Integer> remainingContributions = new HashMap<>();
        contributionsCopy.forEach((key, value) -> {
            if (value > 0) {
                remainingContributions.put(key, value);
            }
        });

        if (!remainingContributions.isEmpty()) {
            Pot finalPot = new Pot(
                    pots.isEmpty() ? PotType.MAIN : PotType.SIDE,
                    remainingContributions,
                    players.stream()
                            .filter(player -> !player.isFolded() && remainingContributions.containsKey(player.getId()))
                            .map(Player::getId)
                            .collect(Collectors.toSet())
            );

            if (finalPot.eligible().isEmpty()) {
                throw  new IllegalArgumentException("There should always be at least one eligible");
            }

            pots.add(finalPot);
            remainingContributions.forEach((key, value) -> contributionsCopy.put(key, contributionsCopy.get(key) - value));
        }

        if (contributionsCopy.values().stream().anyMatch(value -> value > 0)) {
            throw new IllegalArgumentException("Flat contributions must be all zeros after calculating pots");
        }

        return pots;
    }


    public boolean isBetPlaced() {
        return lastFullRaise > 0;
    }

    public int getPlayerContribution(Player player) {
        return contributions.getOrDefault(player.getId(), 0);
    }

    public int getPlayerContribution(String playerId) {
        return contributions.getOrDefault(playerId, 0);
    }

    public int getMaxContribution() {
        return contributions.values().stream().max(Integer::compareTo).orElse(0);
    }

}

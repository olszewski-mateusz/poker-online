package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.pot.Pot;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.history.FlowStep;
import com.molszewski.demos.poker.engine.game.state.history.ShowdownSummaryEntry;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ActionFlow {
    private ActionFlow() {
    }

    static void nextTurn(GameState state) {
        state.getHistory().addFlowStep(FlowStep.NEXT_TURN);
        nextPlayer(state);
        if (state.getPlayers().stream().allMatch(player -> player.isReady() || player.isFolded())) {

            if (state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1 ||
                    state.getGamePhase().equals(GamePhase.DRAWING) &&
                            state.getPlayers().stream().filter(player -> !player.isFolded() && player.getChips() > 0).count() <= 1) {
                transitionToShowdownAndSettle(state);
                return;
            }

            switch (state.getGamePhase()) {
                case FIRST_BETTING -> {
                    state.setGamePhase(GamePhase.DRAWING);
                    state.getPlayers().forEach(player -> player.setReady(false));
                }
                case DRAWING -> {
                    state.setGamePhase(GamePhase.SECOND_BETTING);
                    state.getPlayers().forEach(player -> {
                        if (player.getChips() > 0) {
                            player.setReady(false);
                        }
                    });
                }
                case SECOND_BETTING -> {
                    transitionToShowdownAndSettle(state);
                }
                default -> throw new IllegalStateException("Unexpected phase: " + state.getGamePhase());
            }
        }
    }

    static void tryStartRound(GameState state, GameConfiguration configuration) {
        state.getHistory().addFlowStep(FlowStep.START_ROUND);
        long playersReady = state.getPlayers().stream().filter(Player::isReady).count();
        if (playersReady >= configuration.minPlayersToStart() && playersReady == state.getPlayers().size()) {
            applyStartGame(state, configuration);
        }
    }

    private static void nextPlayer(GameState state) {
        List<Player> players = state.getPlayers();
        Player currentPlayer = state.getCurrentPlayer();

        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }

        if (currentPlayer == null) {
            throw new IllegalStateException("Current player is null");
        }

        List<Player> playersInGame;
        if (state.getGamePhase().equals(GamePhase.FIRST_BETTING) || state.getGamePhase().equals(GamePhase.SECOND_BETTING)) {
            playersInGame = players.stream()
                    .filter(player -> (!player.isFolded() && player.getChips() > 0) || player == currentPlayer)
                    .toList();
        } else {
            playersInGame = players.stream()
                    .filter(player -> !player.isFolded() || player == currentPlayer)
                    .toList();
        }

        state.setCurrentPlayer(playersInGame.get((playersInGame.indexOf(currentPlayer) + 1) % playersInGame.size()));
    }

    private static void applyStartGame(GameState state, GameConfiguration configuration) {
        if (state.getGamePhase().equals(GamePhase.SHOWDOWN)) {
            if (hasCommittedBets(state)) {
                distributePots(state);
            } else if (state.getWinners().isEmpty()) {
                throw new IllegalStateException("No winner found in game");
            }
            reorderPlayers(state);
        }
        state.getPlayers().forEach(player -> player.setFolded(false));
        state.getPlayers().forEach(player -> player.setReady(false));
        disablePlayersWithoutChips(state);
        if (isGameFinished(state)) {
            state.setCurrentPlayer(state.getPlayers().stream().filter(player -> !player.isFolded()).findFirst().orElse(null));
            state.setGamePhase(GamePhase.FINISHED);
            return;
        }
        state.setGamePhase(GamePhase.FIRST_BETTING);
        giveCardsToPlayers(state);
        collectAnte(state, configuration);
        markPlayersWithoutChipsReady(state);
        state.setCurrentPlayer(findCurrentPlayerForBetting(state));

        // When everyone is all-in after ante, skip betting and continue with draw.
        if (state.getCurrentPlayer() == null) {
            state.setGamePhase(GamePhase.DRAWING);
            state.getPlayers().stream()
                    .filter(player -> !player.isFolded())
                    .forEach(player -> player.setReady(false));
            state.setCurrentPlayer(state.getPlayers().stream()
                    .filter(player -> !player.isFolded())
                    .findFirst()
                    .orElse(null));
        }
    }

    private static boolean isGameFinished(GameState state) {
        return state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1;
    }

    private static boolean hasCommittedBets(GameState state) {
        return state.getPlayers().stream().anyMatch(player -> player.getBet() > 0);
    }

    private static void transitionToShowdownAndSettle(GameState state) {
        state.setGamePhase(GamePhase.SHOWDOWN);
        distributePots(state);
        state.getPlayers().forEach(player -> player.setReady(false));
        state.setCurrentPlayer(null);
    }

    private static void distributePots(GameState state) {
        if (state.getWinners().isEmpty()) {
            throw new IllegalStateException("No winner found in game");
        }

        List<Pot> pots = buildPotsFromCommittedBets(state);
        List<ShowdownSummaryEntry.PotSummary> potSummaries = new ArrayList<>();
        Map<String, ShowdownSummaryEntry.PlayerSummary> winnerSummaries = new LinkedHashMap<>();
        for (int i = 0; i < pots.size(); i++) {
            Pot pot = pots.get(i);
            List<Player> potWinners = resolvePotWinners(pot);
            List<String> winnerIds = potWinners.stream().map(Player::getId).toList();
            state.getHistory().addPotWinner(
                    i + 1,
                    pot.amount(),
                    winnerIds
            );
            potSummaries.add(new ShowdownSummaryEntry.PotSummary(i + 1, pot.amount(), winnerIds));
            for (Player winner : potWinners) {
                winnerSummaries.putIfAbsent(
                        winner.getId(),
                        new ShowdownSummaryEntry.PlayerSummary(
                                winner.getId(),
                                winner.getHand() == null ? null : winner.getHand().getHandType(),
                                winner.getHand() == null ? List.of() : winner.getHand().getKeyCards()
                        )
                );
            }
            distributePot(state, pot, potWinners);
        }
        if (winnerSummaries.isEmpty()) {
            state.getWinners().forEach(winner -> winnerSummaries.putIfAbsent(
                    winner.getId(),
                    new ShowdownSummaryEntry.PlayerSummary(
                            winner.getId(),
                            winner.getHand() == null ? null : winner.getHand().getHandType(),
                            winner.getHand() == null ? List.of() : winner.getHand().getKeyCards()
                    )
            ));
        }
        winnerSummaries.values().forEach(winner ->
                state.getHistory().addWinner(winner.playerId(), winner.handType(), winner.keyCards())
        );
        state.getHistory().addShowdownSummary(potSummaries, winnerSummaries.values().stream().toList());

        state.getPlayers().forEach(Player::clearAndGetBet);
    }

    private static List<Pot> buildPotsFromCommittedBets(GameState state) {
        List<Player> players = state.getPlayers();
        List<Integer> potLevels = players.stream()
                .map(Player::getBet)
                .filter(committedBet -> committedBet > 0)
                .distinct()
                .sorted()
                .toList();

        List<Pot> pots = new ArrayList<>();
        int previousPotLevel = 0;
        for (Integer currentPotLevel : potLevels) {
            int currentPotUnit = currentPotLevel - previousPotLevel;
            if (currentPotUnit <= 0) {
                continue;
            }

            List<Player> potContributors = players.stream()
                    .filter(player -> player.getBet() >= currentPotLevel)
                    .toList();
            int potAmount = potContributors.size() * currentPotUnit;
            if (potAmount == 0) {
                previousPotLevel = currentPotLevel;
                continue;
            }

            List<Player> eligiblePlayers = potContributors.stream()
                    .filter(player -> !player.isFolded())
                    .toList();
            if (eligiblePlayers.isEmpty()) {
                throw new IllegalStateException("No eligible players for a non-empty pot");
            }

            pots.add(new Pot(potAmount, potContributors, eligiblePlayers));
            previousPotLevel = currentPotLevel;
        }
        return pots;
    }

    private static List<Player> resolvePotWinners(Pot pot) {
        List<Player> eligiblePlayers = pot.eligiblePlayers();
        if (eligiblePlayers.size() == 1) {
            return eligiblePlayers;
        }

        if (eligiblePlayers.stream().anyMatch(player -> player.getHand() == null)) {
            throw new IllegalStateException("Cannot resolve winner without hand during showdown");
        }

        Hand bestHand = eligiblePlayers.stream()
                .map(Player::getHand)
                .max(Hand::compareTo)
                .orElseThrow();

        return eligiblePlayers.stream()
                .filter(player -> player.getHand().compareTo(bestHand) == 0)
                .toList();
    }

    private static void distributePot(GameState state, Pot pot, List<Player> winners) {
        int potAmount = pot.amount();
        int splitShare = potAmount / winners.size();
        int remaining = potAmount % winners.size();
        winners.forEach(player -> player.addChips(splitShare));

        if (remaining == 0) {
            return;
        }

        Set<Player> winnersSet = new HashSet<>(winners);
        for (Player player : state.getPlayers()) {
            if (remaining == 0) {
                break;
            }
            if (winnersSet.contains(player)) {
                player.addChips(1);
                remaining--;
            }
        }
    }

    private static void disablePlayersWithoutChips(GameState state) {
        state.getPlayers().stream()
                .filter(player -> player.getChips() == 0)
                .forEach(player -> player.setFolded(true));
    }

    private static void reorderPlayers(GameState state) {
        List<Player> players = state.getPlayers();
        players.addLast(players.removeFirst());
    }

    private static void giveCardsToPlayers(GameState state) {
        for (Player player : state.getPlayers()) {
            if (player.getHand() != null) {
                state.getDeck().addAllToDiscards(player.getHand().getCards());
                player.setHand(null);
            }
            if (!player.isFolded()) {
                player.setHand(state.getDeck().getHand());
            }
        }
    }

    private static void collectAnte(GameState state, GameConfiguration configuration) {
        for (Player player : state.getPlayers().stream().filter(player -> !player.isFolded()).toList()) {
            player.transferChipsToBet(Math.min(player.getChips(), configuration.ante()));
        }
    }

    private static void markPlayersWithoutChipsReady(GameState state) {
        state.getPlayers().stream()
                .filter(player -> !player.isFolded() && player.getChips() == 0)
                .forEach(player -> player.setReady(true));
    }

    private static Player findCurrentPlayerForBetting(GameState state) {
        return state.getPlayers().stream()
                .filter(player -> !player.isFolded() && player.getChips() > 0)
                .findFirst()
                .orElse(null);
    }
}

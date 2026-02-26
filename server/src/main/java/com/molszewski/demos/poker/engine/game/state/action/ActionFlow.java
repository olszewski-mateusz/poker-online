package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.history.FlowStep;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.List;

final class ActionFlow {
    private ActionFlow() {
    }

    static void nextTurn(GameState state) {
        state.getHistory().addFlowStep(FlowStep.NEXT_TURN);
        nextPlayer(state);
        if (state.getPlayers().stream().allMatch(player -> player.isReady() || player.isFolded())) {

            if (state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1 ||
                    state.getGamePhase().equals(GamePhase.DRAWING) &&
                            state.getPlayers().stream().filter(player -> !player.isFolded() && player.getMoney() > 0).count() <= 1) {

                state.setGamePhase(GamePhase.SHOWDOWN);
                state.getPlayers().forEach(player -> player.setReady(false));
                state.setCurrentPlayer(null);
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
                        if (player.getMoney() > 0) {
                            player.setReady(false);
                        }
                    });
                }
                case SECOND_BETTING -> {
                    state.setGamePhase(GamePhase.SHOWDOWN);
                    state.getPlayers().forEach(player -> player.setReady(false));
                    state.setCurrentPlayer(null);
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
                    .filter(player -> (!player.isFolded() && player.getMoney() > 0) || player == currentPlayer)
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
            reorderPlayers(state);
            collectBet(state);
        }
        state.getPlayers().forEach(player -> player.setFolded(false));
        state.getPlayers().forEach(player -> player.setReady(false));
        disablePlayersWithoutMoney(state);
        state.setCurrentPlayer(state.getPlayers().stream().filter(player -> !player.isFolded()).findFirst().orElse(null));
        if (isGameFinished(state)) {
            state.setGamePhase(GamePhase.FINISHED);
            return;
        }
        state.setGamePhase(GamePhase.FIRST_BETTING);
        giveCardsToPlayers(state);
        collectAnte(state, configuration);
    }

    private static boolean isGameFinished(GameState state) {
        return state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1;
    }

    private static void collectBet(GameState state) {
        Player winner = state.getWinner().orElseThrow(() -> new IllegalStateException("No winner found in game"));
        int collectedMoney = state.getPlayers().stream().map(Player::clearAndGetBet).reduce(0, Integer::sum);
        winner.addMoney(collectedMoney);
    }

    private static void disablePlayersWithoutMoney(GameState state) {
        state.getPlayers().stream()
                .filter(player -> player.getMoney() == 0)
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
            player.transferMoneyToBet(configuration.ante());
        }
    }
}


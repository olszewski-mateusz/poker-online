package com.molszewski.demos.poker.engine.action.step;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.history.ActionStepType;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.List;

public class StartRound {

    static void tryStartRound(GameState state, GameConfiguration configuration) {
        long playersReady = state.getPlayers().stream().filter(Player::isReady).count();
        if (playersReady >= configuration.minPlayersToStart() && playersReady == state.getPlayers().size()) {
            state.getHistory().addFlowStep(state, ActionStepType.START_ROUND);
            applyStartGame(state, configuration);
        }
    }

    private static void applyStartGame(GameState state, GameConfiguration configuration) {
        if (state.getGamePhase().equals(GamePhase.SHOWDOWN) || state.getGamePhase().equals(GamePhase.FOLDOUT)) {
            reorderPlayers(state);
            state.getPotState().resetToNewRound();
        }
        state.getPlayers().forEach(player -> player.setFolded(false));
        state.getPlayers().forEach(player -> player.setReady(false));
        disablePlayersWithoutChips(state);
        if (isGameFinished(state)) {
            state.setCurrentPlayer(state.getPlayers().stream().filter(player -> !player.isFolded()).findFirst().orElse(null));
            state.setGamePhase(GamePhase.FINISHED);
            state.getHistory().addGameWinner(state, state.getCurrentPlayer());
            return;
        }
        state.setGamePhase(GamePhase.FIRST_BETTING);
        giveCardsToPlayers(state);
        collectAnte(state, configuration);
        markPlayersWithoutChipsReady(state);
        Player currentPlayer = findCurrentPlayerForBetting(state);
        if (currentPlayer != null) {
            state.setCurrentPlayer(currentPlayer);
            return;
        }

        // No player can act in betting (everyone is all-in after ante), skip directly to drawing.
        state.setGamePhase(GamePhase.DRAWING);
        state.getPlayers().stream()
                .filter(player -> !player.isFolded())
                .forEach(player -> player.setReady(false));
        state.setCurrentPlayer(findCurrentPlayerForDrawing(state));
    }

    private static boolean isGameFinished(GameState state) {
        return state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1;
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
            state.getPotState().addAnte(player, Math.min(player.getChips(), configuration.ante()));
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

    private static Player findCurrentPlayerForDrawing(GameState state) {
        return state.getPlayers().stream()
                .filter(player -> !player.isFolded())
                .findFirst()
                .orElse(null);
    }
}

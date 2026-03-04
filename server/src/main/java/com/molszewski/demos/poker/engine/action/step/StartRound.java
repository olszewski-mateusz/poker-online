package com.molszewski.demos.poker.engine.action.step;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.history.ActionStepType;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;

import java.util.List;

public class StartRound {

    static void tryStartRound(GameState state, GameConfiguration configuration) {
        long playersReady = state.getPlayerState().readySize();
        if (playersReady >= configuration.minPlayersToStart() && playersReady == state.getPlayerState().size()) {
            state.getHistory().addFlowStep(state, ActionStepType.START_ROUND);
            applyStartGame(state, configuration);
        }
    }

    private static void applyStartGame(GameState state, GameConfiguration configuration) {
        PlayerState playerState = state.getPlayerState();
        if (state.getGamePhase().equals(GamePhase.SHOWDOWN) || state.getGamePhase().equals(GamePhase.FOLDOUT)) {
            playerState.reorderPlayers();
            state.getPotState().resetToNewRound();
        }
        playerState.removeFoldedForAll();
        playerState.removeReadyForAll();
        playerState.disablePlayersWithoutChips();
        if (isGameFinished(playerState)) {
            playerState.setFinishedPhaseCurrentPlayer();
            state.setGamePhase(GamePhase.FINISHED);
            state.getHistory().addGameWinner(state, playerState.getCurrentPlayer());
            return;
        }
        state.setGamePhase(GamePhase.FIRST_BETTING);
        giveCardsToPlayers(state);
        collectAnte(state, configuration);
        playerState.markPlayersWithoutChipsReady();
        playerState.setFirstPlayerForBetting();
    }

    private static boolean isGameFinished(PlayerState state) {
        return state.getAllNotFolded().size() == 1;
    }

    private static void giveCardsToPlayers(GameState state) {
        for (Player player : state.getPlayerState().getAll()) {
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
        for (Player player : state.getPlayerState().getAllNotFolded()) {
            state.getPotState().addAnte(player, Math.min(player.getChips(), configuration.ante()));
        }
    }
}

package com.molszewski.demos.poker.engine.action.step;

import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.history.ActionStepType;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;
import com.molszewski.demos.poker.engine.pot.Pot;
import com.molszewski.demos.poker.engine.pot.PotType;

import java.util.List;

class NextTurn {
    static void nextTurn(GameState state) {
        PlayerState playerState = state.getPlayerState();

        playerState.setNextCurrentPlayer(state.getGamePhase());
        if (canProceedToNextPhase(playerState)) {

            processOnePlayerSidePots(state);

            if (onlyOnePlayerLeftInRound(playerState))  {
                transitionToFoldoutPhase(state);
                return;
            }

            switch (state.getGamePhase()) {
                case FIRST_BETTING -> transitionToDrawingPhase(state);
                case DRAWING -> {
                    if (isBettingPossible(playerState)) {
                        transitionToSecondBettingPhase(state);
                    } else {
                        transitionToShowdownPhase(state);
                    }
                }
                case SECOND_BETTING -> transitionToShowdownPhase(state);
                default -> throw new IllegalStateException("Unexpected phase: " + state.getGamePhase());
            }
        } else {
            state.getHistory().addFlowStep(state, ActionStepType.NEXT_TURN);
        }
    }

    private static boolean canProceedToNextPhase(PlayerState state) {
        return state.allMatched(player -> player.isReady() || player.isFolded());
    }

    private static boolean onlyOnePlayerLeftInRound(PlayerState state) {
        return state.filtered((player -> !player.isFolded())).size() == 1;
    }

    private static boolean isBettingPossible(PlayerState state) {
        return state.filtered(player -> !player.isFolded() && player.getChips() > 0).size() >= 2;
    }

    private static void transitionToDrawingPhase(GameState state) {
        state.setGamePhase(GamePhase.DRAWING);
        state.getPlayerState().removeReadyForAll();
        state.getPotState().resetToNewBettingPhase();
    }

    private static void transitionToSecondBettingPhase(GameState state) {
        state.setGamePhase(GamePhase.SECOND_BETTING);
        state.getPlayerState().removeReadyForAllWithChips();
    }

    private static void transitionToShowdownPhase(GameState state) {
        state.setGamePhase(GamePhase.SHOWDOWN);
        distributePotsAmongWinners(state, true);
        state.getPlayerState().removeReadyForAll();
        state.getPlayerState().clearCurrentPlayer();
    }

    private static void transitionToFoldoutPhase(GameState state) {
        state.setGamePhase(GamePhase.FOLDOUT);
        distributePotsAmongWinners(state, false);
        state.getPlayerState().removeReadyForAll();
        state.getPlayerState().clearCurrentPlayer();
    }

    private static void distributePotsAmongWinners(GameState state, boolean showdown) {
        for (Pot pot : state.getPotState().calculatePots(state.getPlayerState().getAll())) {
            List<Player> winners = state.getPlayerState().getPotWinners(pot.eligible());
            int amount = pot.getAmount();
            winners.forEach(player -> player.addChips(amount / winners.size())); // todo: better handling odd chips
            state.getHistory().addPotWinner(state, pot, winners, showdown);
        }
    }

    private static void processOnePlayerSidePots(GameState state) {
        for (Pot pot : state.getPotState().calculatePots(state.getPlayerState().getAll())) {
            if (pot.type().equals(PotType.SIDE) && pot.eligible().size() == 1) {
                String eligible = pot.eligible().iterator().next();
                Player winner = state.getPlayerState().getById(eligible);
                winner.addChips(pot.getAmount());
                state.getHistory().addPotWinner(state, pot, List.of(winner), false);
                state.getPotState().removePot(pot);
            }
        }
    }
}

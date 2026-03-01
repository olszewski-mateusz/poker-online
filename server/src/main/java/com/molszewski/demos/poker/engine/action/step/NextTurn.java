package com.molszewski.demos.poker.engine.action.step;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.history.FlowStep;
import com.molszewski.demos.poker.engine.pot.Pot;
import com.molszewski.demos.poker.engine.pot.PotType;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.List;
import java.util.stream.Collectors;

class NextTurn {
    static void nextTurn(GameState state) {
        state.getHistory().addFlowStep(FlowStep.NEXT_TURN);
        nextPlayer(state);
        if (canProceedToNextPhase(state)) {

            processOnePlayerSidePots(state);

            if (onlyOnePlayerLeftInRound(state))  {
                transitionToFoldoutPhase(state);
                return;
            }

            switch (state.getGamePhase()) {
                case FIRST_BETTING -> transitionToDrawingPhase(state);
                case DRAWING -> {
                    if (isBettingPossible(state)) {
                        transitionToSecondBettingPhase(state);
                    } else {
                        transitionToShowdownPhase(state);
                    }
                }
                case SECOND_BETTING -> transitionToShowdownPhase(state);
                default -> throw new IllegalStateException("Unexpected phase: " + state.getGamePhase());
            }
        }
    }

    private static boolean canProceedToNextPhase(GameState state) {
        return state.getPlayers().stream().allMatch(player -> player.isReady() || player.isFolded());
    }

    private static boolean onlyOnePlayerLeftInRound(GameState state) {
        return state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1;
    }

    private static boolean isBettingPossible(GameState state) {
        return state.getPlayers().stream().filter(player -> !player.isFolded() && player.getChips() > 0).count() >= 2;
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

    private static void transitionToDrawingPhase(GameState state) {
        state.setGamePhase(GamePhase.DRAWING);
        state.getPlayers().forEach(player -> player.setReady(false));
        state.getPotState().resetToNewBettingPhase();
    }

    private static void transitionToSecondBettingPhase(GameState state) {
        state.setGamePhase(GamePhase.SECOND_BETTING);
        state.getPlayers().forEach(player -> {
            if (player.getChips() > 0) {
                player.setReady(false);
            }
        });
    }

    private static void transitionToShowdownPhase(GameState state) {
        state.setGamePhase(GamePhase.SHOWDOWN);
        distributePotsAmongWinners(state, true);
        state.getPlayers().forEach(player -> player.setReady(false));
        state.setCurrentPlayer(null);
    }

    private static void transitionToFoldoutPhase(GameState state) {
        state.setGamePhase(GamePhase.FOLDOUT);
        distributePotsAmongWinners(state, false);
        state.getPlayers().forEach(player -> player.setReady(false));
        state.setCurrentPlayer(null);
    }

    private static void distributePotsAmongWinners(GameState state, boolean showdown) {
        for (Pot pot : state.getPotState().calculatePots(state.getPlayers())) {
            List<Player> potPlayers = state.getPlayers().stream().filter(player -> pot.eligible().contains(player.getId())).toList();
            Hand bestHand = potPlayers.stream()
                    .map(Player::getHand)
                    .max(Hand::compareTo)
                    .orElseThrow();

            List<Player> winners = potPlayers.stream().filter(player -> player.getHand().compareTo(bestHand) == 0).toList();
            int amount = pot.getAmount();
            winners.forEach(player -> player.addChips(amount / winners.size())); // todo: better handling odd chips

            state.getHistory().addPotWinner(amount, pot.type(), winners.stream().map(Player::getId).collect(Collectors.toSet()), showdown);
        }
    }

    private static void processOnePlayerSidePots(GameState state) {
        for (Pot pot : state.getPotState().calculatePots(state.getPlayers())) {
            if (pot.type().equals(PotType.SIDE) && pot.eligible().size() == 1) {
                String eligible = pot.eligible().iterator().next();
                state.getPlayerById(eligible).addChips(pot.getAmount());
                state.getHistory().addPotWinner(pot.getAmount(), pot.type(), pot.eligible(), false);
                state.getPotState().removePot(pot);
            }
        }
    }
}

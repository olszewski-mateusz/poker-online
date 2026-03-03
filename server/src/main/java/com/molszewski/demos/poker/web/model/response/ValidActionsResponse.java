package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.action.ActionType;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ValidActionsResponse(List<ValidAction> validActions) {
    public record ValidAction(ActionType type, Map<String, String> properties) {}

    public static ValidActionsResponse fromGameState(String myId, GameState gameState, GameConfiguration configuration) {
        if (myId == null || gameState == null || configuration == null) {
            throw new IllegalStateException("Game state or configuration null");
        }

        List<ValidAction> actions = new ArrayList<>();
        boolean playerExists = gameState.playerExists(myId);

        if (!playerExists) {
            throw new IllegalStateException("My player does not exist");
        }

        Player me = gameState.getPlayerById(myId);
        GamePhase phase = gameState.getGamePhase();

        switch (phase) {
            case NOT_STARTED, SHOWDOWN, FOLDOUT, FINISHED -> {
                actions.add(new ValidAction(ActionType.READY, Map.of("ready", String.valueOf(!me.isReady()))));
            }
            case DRAWING -> {
                if (isCurrentPlayerAbleToAct(myId, gameState)) {
                    actions.add(new ValidAction(ActionType.REPLACE, Map.of(
                            "min", "0",
                            "max", "5"
                    )));
                }
            }
            case FIRST_BETTING, SECOND_BETTING -> {
                if (!isCurrentPlayerAbleToAct(myId, gameState)) {
                    break;
                }

                int playerContribution = gameState.getPotState().getPlayerContribution(me);
                int maxAllInTo = playerContribution + me.getChips();

                if (gameState.getPotState().isBetPlaced()) {
                    int maxContribution = gameState.getPotState().getMaxContribution();
                    int amountToCall = maxContribution - playerContribution;

                    if (amountToCall <= me.getChips()) {
                        actions.add(new ValidAction(ActionType.CALL, Map.of()));
                    }

                    int minRaiseTo = maxContribution + gameState.getPotState().getLastFullRaise();
                    if (maxAllInTo >= minRaiseTo) {
                        actions.add(new ValidAction(ActionType.RAISE, Map.of(
                                "min", String.valueOf(minRaiseTo),
                                "max", String.valueOf(maxAllInTo)
                        )));
                    }
                } else {
                    actions.add(new ValidAction(ActionType.CHECK, Map.of()));
                    int minBetTo = playerContribution + configuration.minOpenBet();
                    if (maxAllInTo >= minBetTo) {
                        actions.add(new ValidAction(ActionType.BET, Map.of(
                                "min", String.valueOf(minBetTo),
                                "max", String.valueOf(maxAllInTo)
                        )));
                    }
                }

                actions.add(new ValidAction(ActionType.ALL_IN, Map.of()));
                actions.add(new ValidAction(ActionType.FOLD, Map.of()));
            }
        }

        return new ValidActionsResponse(List.copyOf(actions));
    }

    private static boolean isCurrentPlayerAbleToAct(String myId, GameState gameState) {
        Player currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.getId().equals(myId)) {
            return false;
        }

        return !currentPlayer.isReady() && !currentPlayer.isFolded();
    }
}

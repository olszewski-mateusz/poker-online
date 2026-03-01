package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.action.*;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.action.*;
import com.molszewski.demos.poker.engine.game.state.history.*;
import com.molszewski.demos.poker.engine.history.*;

public record HistoryEntryResponse(
        String entryType,
        Object details
) {
    public static HistoryEntryResponse fromHistoryEntry(
            HistoryEntry entry,
            GameState gameState
    ) {
        if (entry instanceof ActionEntry actionEntry) {
            return fromAction(actionEntry.getAction(), gameState.getPlayerById(actionEntry.getAction().getPlayerId()).getName());
        }
        if (entry instanceof PhaseChangeEntry phaseChangeEntry) {
            return new HistoryEntryResponse("PHASE_CHANGE", phaseChangeEntry.getPhase());
        }
        if (entry instanceof WinnerEntry winnerEntry) {
            return new HistoryEntryResponse(
                    "WINNER",
                    new WinnerDetails(
                            gameState.getPlayerById(winnerEntry.getPlayerId()).getName()
                    )
            );
        }
        if (entry instanceof GameWinnerEntry winnerEntry) {
            return new HistoryEntryResponse(
                    "GAME_WINNER",
                    new GameWinnerDetails(gameState.getPlayerById(winnerEntry.getPlayerId()).getName())
            );
        }
        if (entry instanceof PotWinnerEntry potWinnerEntry) {
            return new HistoryEntryResponse(
                    "POT_WINNER",
                    new PotWinnerDetails(
                            potWinnerEntry.getPotAmount()
                    )
            );
        }
        return null;
    }

    private static HistoryEntryResponse fromAction(Action action, String playerName) {
        return switch (action) {
            case Join ignored -> new HistoryEntryResponse("JOIN", new ActionDetails(playerName, null));
            case AllIn ignored -> new HistoryEntryResponse("ALL_IN", new ActionDetails(playerName, null));
            case Check ignored -> new HistoryEntryResponse("CHECK", new ActionDetails(playerName, null));
            case Call ignored -> new HistoryEntryResponse("CALL", new ActionDetails(playerName, null));
            case Bet bet -> new HistoryEntryResponse("BET", new ActionDetails(playerName, bet.getAmount()));
            case Fold ignored -> new HistoryEntryResponse("FOLD", new ActionDetails(playerName, null));
            case Raise raise -> new HistoryEntryResponse("RAISE", new ActionDetails(playerName, raise.getAmount()));
            case Ready ready -> new HistoryEntryResponse("READY", new ActionDetails(playerName, ready.isReady()));
            case Replace replace ->
                    new HistoryEntryResponse("REPLACE", new ActionDetails(playerName, replace.getCardsToReplace().size()));
        };
    }

    public record ActionDetails(String playerName, Object value) {
    }

    public record WinnerDetails(String playerName) {
    }

    public record GameWinnerDetails(String playerName) {
    }

    public record PotWinnerDetails(int potAmount) {
    }
}

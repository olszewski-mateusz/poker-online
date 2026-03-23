package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.action.*;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public class ActionEntry extends HistoryEntry<Action> {

    public ActionEntry(GameState gameState, Action action) {
        super(gameState, action);
    }

    @Override
    protected Map<String, String> buildProperties(GameState gameState, Action action) {
        Map<String, String> properties = new HashMap<>();

        Player player = gameState.getPlayerState().getById(action.getPlayerId());

        properties.put("playerName", player.getName());
        switch (action) {
            case AllIn allIn -> {
                properties.put("actionType", ActionType.ALL_IN.toString());
            }
            case Bet bet -> {
                properties.put("actionType", ActionType.BET.toString());
                properties.put("betAmount", String.valueOf(bet.getAmount()));
            }
            case Call call -> {
                properties.put("actionType", ActionType.CALL.toString());
            }
            case Check check -> {
                properties.put("actionType", ActionType.CHECK.toString());
            }
            case Fold fold -> {
                properties.put("actionType", ActionType.FOLD.toString());
            }
            case Join join -> {
                properties.put("actionType", ActionType.JOIN.toString());
            }
            case AiJoin join -> {
                properties.put("actionType", ActionType.AI_JOIN.toString());
            }
            case RemovePlayer removePlayer -> {
                properties.put("actionType", ActionType.REMOVE_PLAYER.toString());
                properties.put("playerName", removePlayer.getTargetPlayer(gameState).getName());
            }
            case ResetGame resetGame -> {
                properties.put("actionType", ActionType.RESET_GAME.toString());
            }
            case Raise raise -> {
                properties.put("actionType", ActionType.RAISE.toString());
                properties.put("raiseAmount", String.valueOf(raise.getAmount()));
            }
            case Ready ready -> {
                properties.put("actionType", ActionType.READY.toString());
                properties.put("ready", String.valueOf(ready.isReady()));
            }
            case Replace replace -> {
                properties.put("actionType", ActionType.REPLACE.toString());
                properties.put("replaceAmount", String.valueOf(replace.getCardsToReplace().size()));
            }
        }

        return properties;
    }

    @Override
    public String getEntryCategory() {
        return "ACTION";
    }
}

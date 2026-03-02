package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.action.*;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;
import lombok.extern.java.Log;
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

        Player player = gameState.getPlayerById(action.getPlayerId());

        properties.put("playerName", player.getName());
        switch (action) {
            case AllIn allIn -> {
                properties.put("actionType", "allIn");
            }
            case Bet bet -> {
                properties.put("actionType", "bet");
                properties.put("betAmount", String.valueOf(bet.getAmount()));
            }
            case Call call -> {
                properties.put("actionType", "call");
            }
            case Check check -> {
                properties.put("actionType", "check");
            }
            case Fold fold -> {
                properties.put("actionType", "fold");
            }
            case Join join -> {
                properties.put("actionType", "join");
            }
            case Raise raise -> {
                properties.put("actionType", "raise");
                properties.put("raiseAmount", String.valueOf(raise.getAmount()));
            }
            case Ready ready -> {
                properties.put("actionType", "ready");
                properties.put("isReady", String.valueOf(ready.isReady()));
            }
            case Replace replace -> {
                properties.put("actionType", "replace");
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

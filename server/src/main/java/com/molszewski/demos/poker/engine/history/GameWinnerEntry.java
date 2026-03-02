package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;

import java.util.Map;

@Getter
class GameWinnerEntry extends HistoryEntry<Player> {

    GameWinnerEntry(GameState gameState, Player player) {
        super(gameState, player);
    }

    @Override
    public String getEntryCategory() {
        return "GAME_WINNER";
    }

    @Override
    protected Map<String, String> buildProperties(GameState gameState, Player player) {
        return Map.of("winnerName", player.getName());
    }
}

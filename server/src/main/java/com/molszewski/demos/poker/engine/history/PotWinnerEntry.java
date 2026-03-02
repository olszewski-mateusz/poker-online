package com.molszewski.demos.poker.engine.history;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.pot.Pot;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
class PotWinnerEntry extends HistoryEntry<PotWinnerEntry.Context> {

    PotWinnerEntry(GameState gameState, Context context) {
        super(gameState, context);
    }

    @Override
    public String getEntryCategory() {
        return "POT_WINNER";
    }

    @Override
    protected Map<String, String> buildProperties(GameState gameState, Context context) {
        return new HashMap<>(Map.of(
                "potType", context.pot.type().toString(),
                "potChips", String.valueOf(context.pot.getAmount()),
                "winners", String.join(",", context.winners.stream().map(Player::getName).toList()),
                "handType", context.winners.getFirst().getHand().getHandType().toString(), // todo: hide if no showdown
                "showdown", String.valueOf(context.showdown)
        ));
    }

    record Context(Pot pot, List<Player> winners, boolean showdown) {
    }
}

package com.molszewski.demos.poker.engine.game.pot;

import com.molszewski.demos.poker.engine.player.Player;

import java.util.List;

public record Pot(
        int amount,
        List<Player> contributors,
        List<Player> eligiblePlayers
) {
}

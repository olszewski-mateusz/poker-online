package com.molszewski.demos.poker.web.collector.history;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.web.collector.history.entry.ActionEntry;
import com.molszewski.demos.poker.web.collector.history.entry.HistoryEntry;
import com.molszewski.demos.poker.web.collector.history.entry.PhaseChangeEntry;
import com.molszewski.demos.poker.web.collector.history.entry.WinnerEntry;
import com.molszewski.demos.poker.web.collector.metadata.MetadataCollector;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HistoryCollector {

    private GamePhase previousPhase;

    private final List<HistoryEntry> entries = new ArrayList<>();

    private HistoryCollector() {
    }

    public static HistoryCollector newInstance() {
        return new HistoryCollector();
    }

    public void includeCommand(Command command, MetadataCollector metadataCollector, Game game) {
        this.entries.add(ActionEntry.fromCommand(command, metadataCollector.getPlayerMetadata(command.getPlayerId()).name()));

        if (!game.getPhase().equals(this.previousPhase)) {
            this.entries.add(PhaseChangeEntry.fromPhase(game.getPhase()));

            if (game.getPhase().equals(GamePhase.SHOWDOWN)) {
                Player winner = game.getGameState().getWinner();
                String winnerName = metadataCollector.getPlayerMetadata(winner.getId()).name();
                this.entries.add(WinnerEntry.create(winnerName, winner.getHand().getHandType()));
            }
        }

        this.previousPhase = game.getPhase();
    }
}

package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.game.GameEngine;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.web.collector.history.entry.HistoryEntry;
import com.molszewski.demos.poker.web.collector.metadata.MetadataCollector;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public record GameResponse(
        String gameId,
        String myId,
        Integer myIndex,
        GamePhase phase,
        Integer currentPlayerIndex,
        Integer winnerIndex,
        List<PlayerResponse> players,
        List<HistoryEntry> history,
        int cardsInDeck,
        int discardedCards,
        boolean betPlacedInCurrentPhase,
        GameConfiguration configuration
) {
    public static GameResponse fromParams(Params params) {

        boolean isShowdown = params.gameEngine.getPhase().equals(GamePhase.SHOWDOWN);

        Integer currentPlayerIndex = Optional.ofNullable(params.gameEngine.getCurrentPlayer())
                .map(Player::getId)
                .map(params.metadataCollector::getPlayerIndex)
                .orElse(null);

        Integer winnerIndex = params.gameEngine.getGameState().getWinner()
                .map(Player::getId)
                .map(params.metadataCollector::getPlayerIndex)
                .orElse(null);

        return GameResponse.builder()
                .gameId(params.gameId)
                .myId(params.myId)
                .myIndex(params.metadataCollector.getPlayerIndex(params.myId))
                .phase(params.gameEngine.getPhase())
                .currentPlayerIndex(currentPlayerIndex)
                .winnerIndex(winnerIndex)
                .players(params.gameEngine.getPlayers().stream()
                        .map(player -> {
                            boolean hideCards = !(isShowdown || player.getId().equals(params.myId));
                            return PlayerResponse.fromPlayer(player, params.metadataCollector.getPlayerMetadata(player.getId()), hideCards);
                        })
                        .toList())
                .cardsInDeck(params.gameEngine.getCardsInDeck())
                .discardedCards(params.gameEngine.getDiscardedCards())
                .history(params.history)
                .betPlacedInCurrentPhase(params.metadataCollector.isBetPlacedInCurrentPhase())
                .configuration(params.gameEngine.getConfiguration())
                .build();
    }

    @Builder
    public record Params(
            String gameId,
            String myId,
            GameEngine gameEngine,
            MetadataCollector metadataCollector,
            List<HistoryEntry> history
    ) {
    }
}
